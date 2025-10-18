package com.example.bytestore.data.model.product


import com.google.gson.annotations.SerializedName

data class ListProductsModels(
    val total: Int,
    val pages: Int,
    val first: Int,
    val next: Int,
    val prev: Int,
    val data: List<ProductModel>
)
data class ProductModel (
    val id:Int,
    val name: String,
    val description:String,
    val discount: Float,
    val stock: Int,
    val image: String,
    val model: String,
    @SerializedName("ram_capacity") val ramCapacity: Int,
    @SerializedName("disk_capacity") val  diskCapacity: Int,
    val qualification: Float,
    val brand: String,
    val processor: ProcessorModel,
    val system: OperatingSystemModel,
    val display: DisplayModel
)

data class ProcessorModel (
    val id: Int?=null,
    val brand: String,
    val family: String,
    val model: String,
    val cores: Int,
    val speed: String
)

data class OperatingSystemModel (
    val id: Int?=null,
    val system: String,
    val distribution: String
)

data class DisplayModel (
    val id: Int?=null,
    val size: Float,
    val resolution: String,
    val graphics: String,
    val brand: String
)