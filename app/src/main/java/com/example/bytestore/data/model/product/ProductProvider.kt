package com.example.bytestore.data.model.product

object ProductProvider {

    //=====================================
    //           Productos
    //=====================================

    //mapa de los porductos
    private val productMap = mutableMapOf<Int, ProductModel>()
    private var lastFetchTimeProducts: Long = 0L

    //variables de paginación
    var total = 0
    var pages = 0
    var next: Int? = null
    var prev: Int? = null

    //obtener todos los productos
    fun getFetchProducts(): ListProductsModel? {
        return if (productMap.isEmpty()) null
        else {
            ListProductsModel(
                total = total,
                pages = pages,
                first = 1,
                next = next,
                prev = prev,
                data = productMap.values.toList()
            )
        }
    }

    //buscar producto por id
    fun findProductById(id: Int): ProductModel? = productMap[id]

    //verificar si necesita volver a consultar los productos
    fun needRefreshProducts(intervalMs: Long): Boolean {
        return (System.currentTimeMillis() - lastFetchTimeProducts) > intervalMs
    }

    //obtener productos similares
    fun getSimilarProducts(id: Int): List<ProductModel> {
        val product = productMap[id] ?: return emptyList()

        return productMap.values
            .filter { it.id != id }
            .map { candidate ->
                val relevance =
                    (if (candidate.brand == product.brand) 3 else 0) +
                            (if (candidate.processor.model == product.processor.model) 2 else 0) +
                            (if (candidate.ramCapacity == product.ramCapacity) 1 else 0)

                candidate to relevance
            }
            .filter { it.second > 0 }
            .take(6)
            .map { it.first }
    }

    //obtener porductos por lista de ids
    fun getProductsByIds(ids: List<Int>): List<ProductModel> {
        if (productMap.isEmpty()) return emptyList()

        val result = ids.mapNotNull { id -> productMap[id] }

        //si la lista es diferente retornal vacia (lanzar peticion en el repository)
        return if (result.size == ids.size) result else emptyList()
    }


    //agregar producto
    fun addProduct(product: ProductModel) {
        if (productMap.containsKey(product.id)) return
        val updated = registerSubcategories(product)
        productMap[updated.id] = updated
        lastFetchTimeProducts = System.currentTimeMillis()
    }

    //añadir productos
    fun addProducts(list: List<ProductModel>) {
        list.forEach { addProduct(it) }
    }

    fun setProducts(newData: ListProductsModel) {
        newData.data.forEach { addProduct(it) }
        if (newData.first == 1) {
            total = newData.total
            pages = newData.pages
        }

        next = newData.next
        prev = newData.prev
    }

    //remove producto
    fun removeProduct(id: Int) {
        productMap.remove(id)
    }

    //limpiar prodcutos
    fun clearCachedProducts() {
        productMap.clear()
        total = 0
        next = null
        prev = null
        pages = 0
    }

    //=====================================
    //           Filtros
    //=====================================

    private var cachedProductFilters: ProductFilters? = null
    private var lastFetchTimeFilters: Long = 0L

    fun setFilters(productFilters: ProductFilters) {
        cachedProductFilters = productFilters
        lastFetchTimeFilters = System.currentTimeMillis()
    }

    fun getFilters(): ProductFilters? {
        return cachedProductFilters
    }

    fun needRefreshFilters(intervalMs: Long): Boolean {
        return (System.currentTimeMillis() - lastFetchTimeFilters) > intervalMs
    }

    //comprobar si los dos listas de filtros son diferentes
    fun ProductFilters.isDifferentFrom(filters: ProductFilters): Boolean {
        if (cachedProductFilters == null) return true
        return filters.brands.toSet() != cachedProductFilters!!.brands.toSet() ||
                filters.processors.toSet() != cachedProductFilters!!.processors.toSet() ||
                filters.displays.toSet() != cachedProductFilters!!.displays.toSet()
    }

    //=====================================
    //           subcategorias de productos
    //=====================================

    //ids temporales para las subcategorias, los ids se sincronizan con los del servidor cuando se solicitan sus cruds
    private var tempIdCounter = -1

    private fun nextTempId(): Int = tempIdCounter--

    //mapas de las subcategorias
    private val processorMap = mutableMapOf<Int, ProcessorModel>()
    private val brandMap = mutableMapOf<Int, BrandModel>()
    private val displayMap = mutableMapOf<Int, DisplayModel>()
    private val osMap = mutableMapOf<Int, OperatingSystemModel>()

    //registrar subcategorias
    private fun registerSubcategories(product: ProductModel): ProductModel {

        //marca
        addBrand(product.brand)

        //procesador
        val processorId = addProcessor(product.processor)


        //display
        val displayId = addDisplay(product.display)

        //sistema
        val osId = addOS(product.system)

        //producto
        return product.copy(
            processor = product.processor.copy(id = processorId),
            display = product.display.copy(id = displayId),
            system = product.system.copy(id = osId),
        )
    }

    //registrar marca
    fun addBrand(brand: String) {
        if (brandMap.values.any { it.name == brand }) {
            brandMap.entries.first { it.value.name == brand }.key
        } else {
            val id = nextTempId()
            brandMap[id] = BrandModel(id, brand)
        }
    }

    //agregar marca (nueva)
    fun addBrand(brand: BrandModel) {
        brandMap.putIfAbsent(brand.id, brand)
    }

    //registrar procesador
    fun addProcessor(processor: ProcessorModel): Int {
        val processorId = processor.id ?: nextTempId()
        processorMap.putIfAbsent(
            processorId,
            processor.copy(id = processorId)
        )
        return processorId
    }

    //registrar graficos
    fun addDisplay(display: DisplayModel): Int {
        val displayId = display.id ?: nextTempId()
        displayMap.putIfAbsent(
            displayId,
            display.copy(id = displayId)
        )
        return displayId
    }

    //registrar sistema operativo
    fun addOS(os: OperatingSystemModel): Int {
        val osId = os.id ?: nextTempId()
        osMap.putIfAbsent(
            osId,
            os.copy(id = osId)
        )
        return osId
    }

    //=====================================
    //         remover subcategorias de productos (en cascada)
    //=====================================

    //remover marca
    fun removeBrand(id: Int) {
        brandMap.remove(id) ?: return
        val productRemove = productMap.values.filter { it.brand == brandMap[id]?.name }
        productRemove.forEach { productMap.remove(it.id) }
    }

    //remover procesador
    fun removeProcessor(id: Int) {
        val removed = processorMap.remove(id) ?: return
        val productsToUpdate = productMap.values.filter { it.processor.id == id }
        productsToUpdate.forEach { productMap.remove(it.id) }
    }

    //remover display
    fun removeDisplay(id: Int) {
        val removed = displayMap.remove(id) ?: return
        val productsToUpdate = productMap.values.filter { it.display.id == id }
        productsToUpdate.forEach { productMap.remove(it.id) }
    }

    //remover sistema
    fun removeOperatingSystem(id: Int) {
        val removed = osMap.remove(id) ?: return
        val productsToUpdate = productMap.values.filter { it.system.id == id }
        productsToUpdate.forEach { productMap.remove(it.id) }
    }

    //=====================================
    //           Asignación de id para las subcategorias
    //=====================================

    //asignar id para una marca
    fun syncBrandId(real: BrandModel) {
        // buscar marca registrada por nombre
        val tempEntry = brandMap.entries.firstOrNull { it.value.name == real.name } ?: return

        val tempId = tempEntry.key
        val stored = tempEntry.value

        // si los modelos son distintos, sobrescribir
        val finalBrand = if (stored != real) real else stored.copy(id = real.id)

        // limpiar id temporal
        brandMap.remove(tempId)
        brandMap[real.id] = finalBrand

        // actualizar productos
        productMap.values.forEach { prod ->
            if (prod.brand == stored.name) {
                productMap[prod.id] = prod.copy(brand = finalBrand.name)
            }
        }
    }

    //id para procesador
    fun syncProcessorId(real: ProcessorModel) {
        // buscar por coincidencia de datos
        val tempEntry = processorMap.entries.firstOrNull {
            it.value.family == real.family &&
                    it.value.model == real.model
        } ?: return

        val tempId = tempEntry.key
        val stored = tempEntry.value

        val finalProcessor =
            if (stored != real) real
            else stored.copy(id = real.id)

        processorMap.remove(tempId)
        processorMap[real.id!!] = finalProcessor

        // actualizar en productos
        productMap.values.forEach { p ->
            if (p.processor.model == stored.model && p.processor.family == stored.family) {
                productMap[p.id] = p.copy(processor = finalProcessor)
            }
        }
    }

    //id para display
    fun syncDisplayId(real: DisplayModel) {
        val tempEntry = displayMap.entries.firstOrNull {
            it.value.size == real.size &&
                    it.value.resolution == real.resolution &&
                    it.value.graphics == real.graphics &&
                    it.value.brand == real.brand
        } ?: return

        val tempId = tempEntry.key
        val stored = tempEntry.value

        val finalDisplay = if (stored != real) real else stored.copy(id = real.id)

        displayMap.remove(tempId)
        displayMap[real.id!!] = finalDisplay

        productMap.values.forEach { p ->
            if (p.display.resolution == stored.resolution && p.display.brand == stored.brand && p.display.size == stored.size && p.display.graphics == stored.graphics) {
                productMap[p.id] = p.copy(display = finalDisplay)
            }
        }
    }

    //id para sistema operativo
    fun syncOsId(real: OperatingSystemModel) {
        val tempEntry = osMap.entries.firstOrNull {
            it.value.system == real.system &&
                    it.value.distribution == real.distribution
        } ?: return

        val tempId = tempEntry.key
        val stored = tempEntry.value

        val finalOs = if (stored != real) real else stored.copy(id = real.id)

        osMap.remove(tempId)
        osMap[real.id!!] = finalOs

        productMap.values.forEach { p ->
            if (p.system.distribution == stored.distribution) {
                productMap[p.id] = p.copy(system = finalOs)
            }
        }
    }

    //=====================================
    //           actulizar las subcategorias
    //=====================================

    fun updateBrand(id: Int, b: BrandModel): Boolean {
        return if (brandMap.containsKey(id)) {
            brandMap[id] = b
            true
        } else false
    }

    fun updateProcessor(id: Int, b: ProcessorModel): Boolean {
        return if (processorMap.containsKey(id)) {
            processorMap[id] = b
            true
        } else false
    }

    fun updateDisplay(id: Int, b: DisplayModel): Boolean {
        return if (displayMap.containsKey(id)) {
            displayMap[id] = b
            true
        } else false
    }

    fun updateOS(id: Int, b: OperatingSystemModel): Boolean {
        return if (osMap.containsKey(id)) {
            osMap[id] = b
            true
        } else false
    }
    //=====================================
    //           validar si los ids de las subcategorias no son temporales
    //=====================================

    fun checkAllBrandsIds() = brandMap.keys.all { it >= 0 }
    fun checkAllProcessorsIds() = processorMap.keys.all { it >= 0 }
    fun checkAllDisplaysIds() = displayMap.keys.all { it >= 0 }
    fun checkAllOSIds() = osMap.keys.all { it >= 0 }

    //=====================================
    //           obtener los mapas de las subcategorias
    //=====================================


    fun getProcessors() = processorMap.values.toList()
    fun getBrands() = brandMap.values.toList()
    fun getDisplays() = displayMap.values.toList()
    fun getOperatingSystems() = osMap.values.toList()

    //=====================================
    //           obtener datos de los mapas de subcategorias
    //=====================================

    fun getProcessor(id: Int): ProcessorModel? = processorMap[id]
    fun getBrand(id: Int): BrandModel? = brandMap[id]
    fun getDisplays(id: Int): DisplayModel? = displayMap[id]
    fun getOperatingSystem(id: Int): OperatingSystemModel? = osMap[id]
}
