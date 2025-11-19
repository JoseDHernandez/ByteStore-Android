package com.example.bytestore.ui.product

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Parcelable
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.bytestore.R
import com.example.bytestore.databinding.FragmentProductsBinding
import com.example.bytestore.ui.components.GridSpacingItemDecorator
import com.example.bytestore.ui.product.components.FiltersBottomSheet
import com.example.bytestore.ui.viewmodel.productViewModels.ProductViewModel
import com.example.bytestore.utils.Resource
import com.example.bytestore.utils.topBar


class ProductsFragment : Fragment() {
    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProductViewModel by viewModels()
    private var recyclerState: Parcelable? = null
    //Paginación
    private var currentPage = 1;
    private var hasNextPage = true;
    private var totalPages = 1;
    private var isLoading = false;
    private lateinit var productAdapter: ProductsListAdapter

    //Variables de busuqeda
    private var query: String? = null
    private var filtersQuery: String? = null
    private var orderQuery: Map<String, String> = emptyMap()

    //busqueda de voz
    private lateinit var speechRecognizer: SpeechRecognizer

    private var isListening = false //estado de la escucha (SpeechRecognizer)

    //permisos de microfono
    private val requestAudioPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            searchSpeech()
        } else {
            Toast.makeText(requireContext(), "Permiso de micrófono denegado", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        topBar().setTitle("Productos")
        //filtros
        binding.filters.setOnClickListener {
            showFilters()
        }
        //limpiar busqueda
        binding.resetProductsButton.setOnClickListener {
            clearSearch()
        }
        //congifurar datos a la UI
        setAdapterAndRecyclerView(savedInstanceState)

        //validar permiso al microfono
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            searchSpeech()
        } else {
            //solicitar permiso
            requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
        //logica del boton de busqueda
        searchButtonLogic()
    }

    //busqueda por voz
    @SuppressLint("ClickableViewAccessibility")
    private fun searchSpeech() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        //Configuración
        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Hablar ahora")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,3)// 3 coincidencias
        }
        //listener
        speechRecognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            //Resultado de analisis de voz
            override fun onResults(results: Bundle?) {
                val spokenText = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.lowercase()
                    ?.trim()
                    ?.replace(Regex("[^0-9a-záéíóúñ\\s]"), "")
                    ?: ""
                //filtrado de texto
                val filterSpokenText = spokenText.split(Regex("\\s+"))
                    .filter { it.length > 1 } //eliminar caracteres solos
                    .filter { it.isNotBlank() } //eliminar vacios
                    .distinct() // eliminar palabras repetidas
                    .joinToString(" ")
                //consumir texto filtrado
                binding.searchInput.setText(filterSpokenText)
                performSearch(filterSpokenText)
                stopListeningSafely()
                //ocultar menu
                binding.speechOptions.visibility = View.GONE
            }

            //error de voz
            override fun onError(error: Int) {
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        Toast.makeText(requireContext(), "No se detectó voz, intenta de nuevo", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Error al reconocer voz ($error)", Toast.LENGTH_SHORT).show()
                    }
                }
                stopListeningSafely()
            }

            //otros metodos del RecognitionListener
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        //mostrar menu de hablar
        binding.voiceButton.setOnClickListener {
            binding.speechOptions.visibility = View.VISIBLE
        }
        //boton de hablar
        binding.speechTextRec.setOnTouchListener { v,event ->
            when(event.action){
                //al mantener presionado
                MotionEvent.ACTION_DOWN -> {
                    if (!isListening) {
                        isListening = true
                        //cambiar boton
                        binding.speechTextRec.setBackgroundResource(R.drawable.btn_green_outline_selector)
                        binding.speechTextRec.setColorFilter(
                            ContextCompat.getColor(requireContext(), R.color.dark_green),
                            PorterDuff.Mode.SRC_IN
                        )
                        //escuchar
                        speechRecognizer.startListening(speechIntent)
                        Toast.makeText(requireContext(), "Escuchando...", Toast.LENGTH_SHORT).show()
                    }
                }
                //al levantar o cancelar
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL-> {
                    v.performClick()
                    if(isListening){
                        stopListeningSafely()
                    }
                }
            }
            true
        }
        //cancelar y ocultar menu de hablar
        binding.speechTextClose.setOnClickListener {
            binding.speechOptions.visibility = View.GONE
            stopListeningSafely()
        }
    }
    //detener escucha
    private fun stopListeningSafely() {
        if (isListening) {
            try {
                speechRecognizer.stopListening()
            } catch (_: Exception) {}
            finally {
                isListening = false
                //restaurar boton
                binding.speechTextRec.setBackgroundResource(R.drawable.btn_green_filled_selector)
                binding.speechTextRec.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.white),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
    }
    //logica del boton de buscar
    private fun searchButtonLogic() {
        //detectar boton enter
        binding.searchInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val query = binding.searchInput.text.toString().trim()

                if (query.length in 2..300) {
                    performSearch(query)
                } else {
                    Toast.makeText(requireContext(), "Busqueda vacia", Toast.LENGTH_SHORT).show()
                }
                true
            } else {
                false
            }
        }
        //boton busqueda
        binding.searchButton.setOnClickListener {
            performSearch(binding.searchInput.text.toString().trim())
        }
        //limpiar busqueda
        binding.clearSearchButton.setOnClickListener {
            clearSearch()
        }
        //mostrar limpiar
        binding.searchInput.addTextChangedListener { editable ->
            val text = editable?.toString()?:""
            if(text.length > 1) {
                binding.clearSearchButton.visibility = View.VISIBLE
            } else {
                binding.clearSearchButton.visibility = View.GONE
            }

        }
    }

    private fun performSearch(search: String) {
        query = null
        val searchText = search.trim()
        val searchRegex = Regex("^[0-9A-Za-zÁÉÍÓÚáéíóúÑñ\\s]{2,300}$")
        if (searchRegex.matches(searchText)) {
            //establcer valores
            query = searchText
            search()
        } else {
            Toast.makeText(
                requireContext(),
                "Terminos de busqueda invalidos",
                Toast.LENGTH_SHORT
            ).show()
            viewModel.getProducts(1, null)
        }
    }

    private fun setAdapterAndRecyclerView(savedInstanceState: Bundle?) {
        //Listadapater de prodcutos
        productAdapter = ProductsListAdapter { product ->
            //Callback del onClick
            Log.d("ProductsFragment", "Click: ${product.id}")

            val action =
                ProductsFragmentDirections.actionProductsFragmentToProductFragment(product.id)
            findNavController().navigate(action)
        }
        //configuracion del recycleview
        val recyclerView = binding.productsRecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = productAdapter
        val layoutManager = StaggeredGridLayoutManager(2, RecyclerView.VERTICAL)
        recyclerView.layoutManager = layoutManager
        recyclerView.layoutManager?.onRestoreInstanceState(savedInstanceState)
        //espaciado entre tarjetas
        recyclerView.addItemDecoration(
            GridSpacingItemDecorator(
                2,
                resources.getDimensionPixelSize(R.dimen.grid_spacing),
                false
            )
        )
        //infinte scroll
        infiniteScroll(layoutManager)
        //obtener productos
        observeLiveData()
        //solicitar productos
        viewModel.getProducts()
    }

    @SuppressLint("SetTextI18n")
    private fun observeLiveData() {
        viewModel.productsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Idle -> Unit
                is Resource.Success -> {
                    binding.errorLayout.visibility = View.GONE
                    val result = state.data
                    //paginas totales
                    totalPages = result.pages

                    val current = productAdapter.currentList

                    val currentIds = current.map { it.id }.toSet()
                    val newItems = result.data.filter { it.id !in currentIds }

                    val newList = current + newItems
                    productAdapter.submitList(newList)


                    //estado de la paginacion
                    isLoading = false
                    binding.progressBar.visibility = View.GONE
                    binding.productsRecyclerView.visibility = View.VISIBLE
                }

                is Resource.Error -> {
                    binding.productsRecyclerView.visibility = View.GONE
                    binding.progressBar.visibility = View.GONE
                    binding.errorMessage.text = state.message
                    binding.errorLayout.visibility = View.VISIBLE
                }

                is Resource.Loading -> {
                    binding.productsRecyclerView.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                    if (currentPage == 1) {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }

                is Resource.ValidationError -> {
                    //Opcion sin uso
                    binding.progressBar.visibility = View.GONE
                    binding.errorLayout.visibility = View.GONE
                    binding.productsRecyclerView.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun infiniteScroll(layoutManager: StaggeredGridLayoutManager) {
        binding.productsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0 || isLoading || !hasNextPage) return // scrol hacia arriba

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                //calcular items visibles
                val firstVisibleItemPositions = IntArray(2)
                layoutManager.findFirstVisibleItemPositions(firstVisibleItemPositions)
                val firstVisibleItem = firstVisibleItemPositions.minOrNull() ?: 0

                val endReached = (visibleItemCount + firstVisibleItem) >= totalItemCount - 4

                if (endReached) {
                    loadNextPage()
                }

            }
        })
    }

    private fun loadNextPage() {
        isLoading = true;
        currentPage++ //aumentar pagina
        if (currentPage > totalPages) {
            hasNextPage = false
        } else {
            viewModel.getProducts(currentPage)
        }

    }

    //filtros
    private fun showFilters() {
        val sheet =
            FiltersBottomSheet { selectedFilters, selectedOrder ->
                //busuqeda con filtros
                filtersQuery = selectedFilters.joinToString(" ")
                orderQuery = selectedOrder
                search()
            }
        sheet.show(parentFragmentManager, "FiltersBottomSheet")
    }

    //aplicar filtros/busqueda
    private fun search() {
        //unir texto de busqueda con filtros
        val fullQuery = ((query ?: "") + (filtersQuery ?: "")).lowercase().split("\\s+".toRegex())
            .filter { it.isNotBlank() }.distinct().joinToString(",")
        currentPage = 1
        viewModel.getProducts(currentPage, fullQuery, orderQuery["sort"], orderQuery["order"])
        //subir scroll
        binding.productsRecyclerView.scrollToPosition(0)
    }
    //limpiar busuqeda
    private fun clearSearch(){
        query = ""
        filtersQuery = null
        orderQuery = emptyMap()
        currentPage = 1
        binding.searchInput.setText("")
        viewModel.getProducts(currentPage)
    }

    override fun onPause() {
        super.onPause()
        recyclerState = binding.productsRecyclerView.layoutManager?.onSaveInstanceState()
    }

    override fun onResume() {
        super.onResume()
        recyclerState?.let {
            binding.productsRecyclerView.layoutManager?.onRestoreInstanceState(it)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }
}