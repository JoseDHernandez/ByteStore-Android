package com.example.bytestore.data.network.product

import com.example.bytestore.core.ApiClient
import com.example.bytestore.data.model.product.BrandModel
import com.example.bytestore.data.model.product.DisplayModel
import com.example.bytestore.data.model.product.DisplayRegisterRequest
import com.example.bytestore.data.model.product.DisplayUpdateRequest
import com.example.bytestore.data.model.product.ListProductsModel
import com.example.bytestore.data.model.product.OSRegisterRequest
import com.example.bytestore.data.model.product.OSUpdateRequest
import com.example.bytestore.data.model.product.OperatingSystemModel
import com.example.bytestore.data.model.product.ProcessorModel
import com.example.bytestore.data.model.product.ProcessorRegisterRequest
import com.example.bytestore.data.model.product.ProcessorUpdateRequest
import com.example.bytestore.data.model.product.ProductBrandRequest
import com.example.bytestore.data.model.product.ProductFilters
import com.example.bytestore.data.model.product.ProductModel
import com.example.bytestore.data.model.product.ProductRegisterRequest
import com.example.bytestore.data.model.product.ProductUpdateRequest
import com.example.bytestore.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.Response

class ProductService {
    private val api =
        ApiClient.retrofit().create(ProductApiService::class.java) //Solicito el helper de retrofit

    // ===== Nota ====
    // getProducts y getProduct no retornar Resourse<ListProductsModel>
    // o Resource<ProductModel>, ya que los caso de error son mayormente
    // 404 0 500 y estos se pueden manejar con null. Para peticiones con
    // codigos mas explicitos usar Resource.
    // ===============

    suspend fun getProducts(
        page: Int?,
        limit: Int?,
        search: String?,
        sort: String?,
        order: String?
    ): ListProductsModel? = withContext(Dispatchers.IO) {
        //capto la respuesta en el data model/ dto
        try {
            val response: Response<ListProductsModel> =
                //petición basada en la establecida en el ApiModel
                api.getProducts(
                    page = page,
                    limit = limit,
                    search = search,
                    sort = sort,
                    order = order
                )
            if (response.isSuccessful) response.body() else null //cuerpo de la respuesta
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getProduct(id: Int): ProductModel? = withContext(Dispatchers.IO) {
        try {
            val response: Response<ProductModel> = api.getProduct(id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //obtener productos similares
    suspend fun getSimilarProducts(id: Int): List<ProductModel>? = withContext(Dispatchers.IO) {
        try {
            val response: Response<List<ProductModel>> = api.getSimilarProducts(id)
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //obtener filtros
    suspend fun getProductFilters(): ProductFilters? = withContext(Dispatchers.IO) {
        try {
            val response: Response<ProductFilters> = api.getProductFilters()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    //obtener productos por lista de id
    suspend fun getProductsByIds(ids: String): Resource<List<ProductModel>> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.getProductsByList(ids)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos.")
                    }
                } else {
                    Resource.Error("Error al obtener los productos con ids: $ids")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //registrar producto
    suspend fun registerProduct(request: ProductRegisterRequest): Resource<ProductModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.registerProduct(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos.")
                    }
                } else {
                    Resource.Error("Error al registrar el producto: ${request.name} - ${request.model}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //actuzlizar producto
    suspend fun updateProduct(id: Int, request: ProductUpdateRequest): Resource<ProductModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.updateProduct(id, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos.")
                    }
                } else {
                    Resource.Error("Error al actulizar el producto con el id: $id")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    // eliminar producto
    suspend fun deleteProduct(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProduct(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //===================================
    //            Imagenes
    //===================================

    //subir imagen
    suspend fun uploadImage(file: MultipartBody.Part): Resource<String> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.uploadImage(file)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body.filepath)
                    } else {
                        Resource.Error("Cuerpo sin datos")
                    }

                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                    when (response.code()) {
                        400 -> return@withContext Resource.ValidationError(mapOf("message" to "Imagen no adjunta"))
                        413 -> return@withContext Resource.ValidationError(mapOf("message" to "Imagen superior a 30kb"))
                        415 -> return@withContext Resource.ValidationError(mapOf("message" to "Formato de imagen invalido"))
                        else -> return@withContext Resource.Error("Error ${response.code()}: $errorMessage")
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //actualizar imagen
    suspend fun changeImage(
        filename: String,
        file: MultipartBody.Part
    ): Resource<String> = withContext(
        Dispatchers.IO
    ) {
        try {
            val response = api.changeImage(filename, file)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body.filepath)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }

            } else {
                val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                when (response.code()) {
                    400 -> return@withContext Resource.ValidationError(mapOf("message" to "Imagen no adjunta o no encontrada con el nombre: $filename"))
                    413 -> return@withContext Resource.ValidationError(mapOf("message" to "Imagen superior a 30kb"))
                    415 -> return@withContext Resource.ValidationError(mapOf("message" to "Formato de imagen invalido"))
                    else -> return@withContext Resource.Error("Error ${response.code()}: $errorMessage")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //delete image
    suspend fun deleteImage(filename: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteImage(filename)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //===================================
    //            Marcas
    //===================================

    //obtener marcas
    suspend fun getBrands(): Resource<List<BrandModel>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBrands()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }
            } else {
                Resource.Error("Error al obtener las marcas")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //obtener marca por id
    suspend fun getBrand(id: Int): Resource<BrandModel> = withContext(Dispatchers.IO) {
        try {
            val response = api.getBrandById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }
            } else {
                Resource.Error("Error al obtener la marca con el id: $id")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //registrar marca
    suspend fun registerBrand(request: ProductBrandRequest): Resource<BrandModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.registerBrand(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos")
                    }
                } else {
                    Resource.Error("Error al registrar la marca: ${request.name}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //actualizar marca
    suspend fun updateBrand(id: Int, request: ProductBrandRequest): Resource<BrandModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.updateBrand(id, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos")
                    }
                } else {
                    Resource.Error("Error al actualizar la marca con el id: $id")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //eliminar
    suspend fun deleteBrand(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteBrand(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //===================================
    //            Graficos/displays
    //===================================

    //obtener todos los graficos
    suspend fun getDisplays(): Resource<List<DisplayModel>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDisplays()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }
            } else {
                Resource.Error("Error al obtener los graficos")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //obtener por id
    suspend fun getDisplay(id: Int): Resource<DisplayModel> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDisplayById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }
            } else {
                Resource.Error("Error al obtener los graficos con id: $id")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //registrar
    suspend fun registerDisplay(request: DisplayRegisterRequest): Resource<DisplayModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.registerDisplay(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos")
                    }
                } else {
                    Resource.Error("Error al registrar los graficos: ${request.brand} - ${request.resolution}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //acutlizar
    suspend fun updateDisplay(id: Int, request: DisplayUpdateRequest): Resource<DisplayModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.updateDisplay(id, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos")
                    }
                } else {
                    Resource.Error("Error al actualizar los graficos con id: $id")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //eliminar graficos
    suspend fun deleteDisplay(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteDisplay(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //===================================
    //            procesadores
    //===================================

    //obtener procesadores
    suspend fun getProcessors(): Resource<List<ProcessorModel>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProcessors()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }
            } else {
                Resource.Error("Error al obtener procesadores")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //obtenr por id
    suspend fun getProcessor(id: Int): Resource<ProcessorModel> = withContext(Dispatchers.IO) {
        try {
            val response = api.getProcessorById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }
            } else {
                Resource.Error("Error al obtener el procesador con el id: $id")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //registar procesador
    suspend fun registerProcessor(request: ProcessorRegisterRequest): Resource<ProcessorModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.registerProcessor(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos")
                    }
                } else {
                    Resource.Error("Error al registrar el procesador: ${request.model} ${request.brand}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //actualizar
    suspend fun updateProcessor(
        id: Int,
        request: ProcessorUpdateRequest
    ): Resource<ProcessorModel> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateProcessor(id, request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }
            } else {
                Resource.Error("Error al actualizar el procesador con el id: $id")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //eliminar
    suspend fun deleteProcessor(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteProcessor(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    //===================================
    //            sistemas operativos
    //===================================

    //obtener sistemas operativos
    suspend fun getAllOS(): Resource<List<OperatingSystemModel>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOS()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }
            } else {
                Resource.Error("Error al obtener los sistemas operativos")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //obtener sistema operativo por id
    suspend fun getOS(id: Int): Resource<OperatingSystemModel> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOSById(id)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Resource.Success(body)
                } else {
                    Resource.Error("Cuerpo sin datos")
                }
            } else {
                Resource.Error("Error al obtener el sistema operativo con el id: $id")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Resource.Error("Error de conexión: ${e.localizedMessage}")
        }
    }

    //registrar sistema operativo
    suspend fun registerOS(request: OSRegisterRequest): Resource<OperatingSystemModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.registerOS(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos")
                    }
                } else {
                    Resource.Error("Error al registrar el sistema opertaivo: ${request.system} = ${request.distribution}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //actualizar
    suspend fun updateOS(id: Int, request: OSUpdateRequest): Resource<OperatingSystemModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = api.updateOS(id, request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Cuerpo sin datos")
                    }
                } else {
                    Resource.Error("Error al actulizar el sistema operativo con el id: $id")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Resource.Error("Error de conexión: ${e.localizedMessage}")
            }
        }

    //eliminar
    suspend fun deleteOS(id: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = api.deleteOS(id)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

}