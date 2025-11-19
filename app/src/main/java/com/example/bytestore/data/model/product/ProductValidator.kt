package com.example.bytestore.data.model.product

data class ProductInputs(
    val name: String,
    val description: String,
    val price: Double,
    val discount: Int,
    val stock: Int,
    val image: String,
    val model: String,
    val ramCapacity: Int,
    val diskCapacity: Int,
    val processor: ProcessorInputs,
    val system: SystemInputs,
    val display: DisplayInputs,
    val brand: String
)

data class ProcessorInputs(
    val brand: String,
    val family: String,
    val model: String,
    val cores: Int,
    val speed: String
)

data class SystemInputs(
    val system: String,
    val distribution: String
)

data class DisplayInputs(
    val size: Int,
    val resolution: String,
    val graphics: String,
    val brand: String
)


object ProductValidator {
    //TODO: cambiar regex 

    // =======================
    //        REGEX
    // =======================
    private val nameRegex = Regex("^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñ\\s\\-]+$")
    private val descriptionRegex = Regex("^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñ\\s\\.\\,\\'\\\"\\(\\)\\-\\!¡\\:\\;]+$")
    private val modelRegex = Regex("^[\\w\\d\\-\\/\\]+$")
    private val textRegex = Regex("^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñ\\s\\-]+$")
    private val speedRegex = Regex("^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñ\\s.,\\-)(/]+$")

    // =======================
    //     VALIDACIÓN MAIN
    // =======================
    fun validateProduct(input: ProductInputs): MutableMap<String, String> {
        val errors = mutableMapOf<String, String>()

        // --- Nombre ---
        if (input.name.isBlank()) errors["name"] = "El nombre es obligatorio"
        else if (input.name.length < 5) errors["name"] = "El nombre debe tener al menos 5 caracteres"
        else if (input.name.length > 40) errors["name"] = "El nombre no debe exceder 40 caracteres"
        else if (!nameRegex.matches(input.name)) errors["name"] = "Nombre inválido"

        // --- Descripción ---
        if (input.description.isBlank()) errors["description"] = "La descripción es obligatoria"
        else if (input.description.length < 10) errors["description"] = "La descripción debe tener al menos 10 caracteres"
        else if (input.description.length > 1000) errors["description"] = "La descripción no debe exceder 1000 caracteres"
        else if (!descriptionRegex.matches(input.description)) errors["description"] = "Descripción inválida"

        // --- Precio ---
        if (input.price == null) errors["price"] = "El precio debe ser un número válido"
        else if (input.price < 100000) errors["price"] = "El precio mínimo es 100.000"
        else if (input.price > 20000000) errors["price"] = "El precio máximo es 20.000.000"

        // --- Descuento ---
        if (input.discount == null) errors["discount"] = "El descuento debe ser un número válido"
        else if (input.discount < 0) errors["discount"] = "El descuento mínimo es 0%"
        else if (input.discount > 90) errors["discount"] = "El descuento máximo es 90%"

        // --- Stock ---
        if (input.stock == null) errors["stock"] = "El stock debe ser un número entero"
        else if (input.stock < 0) errors["stock"] = "El stock no puede ser negativo"

        // --- Imagen ---
        if (input.image.isBlank()) errors["image"] = "La imagen es obligatoria"
        else if (!android.util.Patterns.WEB_URL.matcher(input.image).matches())
            errors["image"] = "Debe ser una URL válida"

        // --- Modelo ---
        if (input.model.isBlank()) errors["model"] = "El modelo es obligatorio"
        else if (input.model.length < 5) errors["model"] = "El modelo debe tener al menos 5 caracteres"
        else if (input.model.length > 36) errors["model"] = "El modelo no debe exceder 36 caracteres"
        else if (!modelRegex.matches(input.model)) errors["model"] = "Modelo inválido"

        // --- RAM ---
        if (input.ramCapacity == null) errors["ram_capacity"] = "La RAM debe ser un número válido"
        else if (input.ramCapacity < 8) errors["ram_capacity"] = "La RAM mínima es 8 GB"
        else if (input.ramCapacity > 128) errors["ram_capacity"] = "La RAM máxima es 128 GB"

        // --- Almacenamiento ---
        if (input.diskCapacity == null) errors["disk_capacity"] = "El almacenamiento debe ser un número válido"
        else if (input.diskCapacity < 120) errors["disk_capacity"] = "El almacenamiento mínimo es 120 GB"
        else if (input.diskCapacity > 10000) errors["disk_capacity"] = "El almacenamiento máximo es 10000 GB"

        // --- Marca ---
        if (input.brand.isBlank()) errors["brand"] = "La marca es obligatoria"
        else if (input.brand.length < 2) errors["brand"] = "La marca debe tener al menos 2 caracteres"
        else if (input.brand.length > 50) errors["brand"] = "La marca no debe exceder 50 caracteres"
        else if (!textRegex.matches(input.brand)) errors["brand"] = "Marca inválida"

        // --- Procesador ---
        errors.putAll(validateProcessor(input.processor))

        // --- Sistema operativo ---
        errors.putAll(validateOperatingSystem(input.system))

        // --- Pantalla / Gráficos ---
        errors.putAll(validateDisplay(input.display))

        return errors
    }

    // =======================
    //    VALIDACIÓN ANIDADA
    // =======================

    fun validateProcessor(p: ProcessorInputs): Map<String, String> {
        val e = mutableMapOf<String, String>()

        if (p.brand.isBlank()) e["processor.brand"] = "La marca del procesador es obligatoria"
        else if (p.brand.length < 3) e["processor.brand"] = "La marca debe tener al menos 3 caracteres"
        else if (p.brand.length > 30) e["processor.brand"] = "La marca no debe exceder 30 caracteres"
        else if (!textRegex.matches(p.brand)) e["processor.brand"] = "Marca inválida"

        if (p.family.isBlank()) e["processor.family"] = "La familia del procesador es obligatoria"
        else if (!textRegex.matches(p.family)) e["processor.family"] = "Familia inválida"

        if (p.model.isBlank()) e["processor.model"] = "El modelo del procesador es obligatorio"
        else if (!textRegex.matches(p.model)) e["processor.model"] = "Modelo inválido"

        if (p.cores == null) e["processor.cores"] = "Los núcleos deben ser un número válido"
        else if (p.cores < 4) e["processor.cores"] = "El procesador debe tener mínimo 4 núcleos"
        else if (p.cores > 64) e["processor.cores"] = "No puede tener más de 64 núcleos"

        if (p.speed.isBlank()) e["processor.speed"] = "La velocidad del procesador es obligatoria"
        else if (!speedRegex.matches(p.speed)) e["processor.speed"] = "Velocidad inválida"

        return e
    }

    fun validateOperatingSystem(s: SystemInputs): Map<String, String> {
        val e = mutableMapOf<String, String>()

        if (s.system.isBlank()) e["system.system"] = "El sistema operativo es obligatorio"
        else if (!textRegex.matches(s.system)) e["system.system"] = "Sistema operativo inválido"

        if (s.distribution.isBlank()) e["system.distribution"] = "La distribución es obligatoria"
        else if (!textRegex.matches(s.distribution)) e["system.distribution"] = "Distribución inválida"

        return e
    }

    fun validateDisplay(d: DisplayInputs): Map<String, String> {
        val e = mutableMapOf<String, String>()

        if (d.size == null) e["display.size"] = "El tamaño debe ser un número válido"
        else if (d.size < 10) e["display.size"] = "El tamaño mínimo es 10 pulgadas"
        else if (d.size > 20) e["display.size"] = "El tamaño máximo es 20 pulgadas"

        if (d.resolution.isBlank()) e["display.resolution"] = "La resolución es obligatoria"
        else if (!textRegex.matches(d.resolution)) e["display.resolution"] = "Resolución inválida"

        if (d.graphics.isBlank()) e["display.graphics"] = "Los gráficos son obligatorios"
        else if (!textRegex.matches(d.graphics)) e["display.graphics"] = "Gráficos inválidos"

        if (d.brand.isBlank()) e["display.brand"] = "La marca de los gráficos es obligatoria"
        else if (!textRegex.matches(d.brand)) e["display.brand"] = "Marca inválida"

        return e
    }
}
