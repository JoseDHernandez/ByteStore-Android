package com.example.bytestore.data.model.user

// Campos del registro
data class UserRegisterInputs(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val address: String
)

// Campos de actulizar cuenta
data class UserUpdateInputs(
    val name: String?,
    val email: String?,
    val address: String?
)

object UserValidator {

    private val nameRegex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]+$")
    val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[-A-Za-z0-9._]+\\.[A-Za-z]{2,}$")
    private val passwordRegex =
        Regex("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,20}$")
    private val addressRegex = Regex("^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñ\\s.,'\"#°\\-]+$")

    //Getters

    fun passwordRegex() = passwordRegex
    fun nameRegex() = nameRegex
    fun emailRegex() = emailRegex
    fun addressRegex() = addressRegex

    //=============================================
    //             Validar registro
    //=============================================

    fun validateRegister(input: UserRegisterInputs): MutableMap<String, String> {
        val errors = mutableMapOf<String, String>()

        // Nombre
        val name = input.name.trim()
        when {
            name.length < 6 -> errors["name"] = "Debe tener al menos 6 caracteres"
            name.length > 200 -> errors["name"] = "No puede exceder 200 caracteres"
            !nameRegex.matches(name) -> errors["name"] = "Solo letras y espacios"
        }

        // Correo
        val email = input.email.trim()
        when {
            email.length < 5 -> errors["email"] = "Debe tener al menos 5 caracteres"
            email.length > 300 -> errors["email"] = "No puede exceder 300 caracteres"
            !emailRegex.matches(email) -> errors["email"] = "Formato de correo no válido"
        }

        // Contraseña
        val password = input.password.trim()
        when {
            password.length < 8 -> errors["password"] = "Debe tener al menos 8 caracteres"
            password.length > 20 -> errors["password"] = "No debe exceder 20 caracteres"
            !passwordRegex.matches(password) ->
                errors["password"] = "Debe incluir mayúscula, minúscula, número y carácter especial"
        }

        // Confirmación
        val confirmPass = input.confirmPassword.trim()
        if (confirmPass != password) {
            errors["confirmPassword"] = "Las contraseñas no coinciden"
        }

        // Dirección
        val address = input.address.trim()
        when {
            address.length < 2 -> errors["address"] = "La dirección es muy corta"
            address.length > 100 -> errors["address"] = "No debe exceder 100 caracteres"
            !addressRegex.matches(address) -> errors["address"] =
                "Caracteres inválidos en la dirección"
        }

        return errors
    }


    //=============================================
    //             Validar login
    //=============================================

    fun validateLogin(email: String, password: String): MutableMap<String, String> {
        val errors = mutableMapOf<String, String>()

        // Correo
        val mail = email.trim()
        when {
            mail.length < 5 -> errors["email"] = "Debe tener al menos 5 caracteres"
            mail.length > 300 -> errors["email"] = "No puede exceder 300 caracteres"
            !emailRegex.matches(mail) -> errors["email"] = "Formato de correo no válido"
        }

        // Contraseña
        val pass = password.trim()
        when {
            pass.length < 8 -> errors["password"] = "Debe tener al menos 8 caracteres"
            pass.length > 20 -> errors["password"] = "No debe exceder 20 caracteres"
            !passwordRegex.matches(pass) ->
                errors["password"] = "Debe incluir mayúscula, minúscula, número y carácter especial"
        }

        return errors
    }

    //=============================================
    //             Validar actulizar cuenta
    //=============================================

    fun validateUpdateUser(input: UserUpdateInputs): MutableMap<String, String> {
        val errors = mutableMapOf<String, String>()
        val name = input.name
        val email = input.email
        val address = input.address

        //validar campos
        if (name.isNullOrBlank() && email.isNullOrBlank() && address.isNullOrEmpty()) {
            errors["name"] = "Nombre no ingreasdo"
            errors["email"] = "Correo no ingresado"
            errors["address"] = "Dirección no ingresada"
        }

        //nombre
        if (!name.isNullOrBlank()) {
            when {
                name.length < 6 -> errors["name"] = "Debe tener al menos 6 caracteres"
                name.length > 200 -> errors["name"] = "No puede exceder 200 caracteres"
                !nameRegex.matches(name) -> errors["name"] = "Solo letras y espacios"
            }
        }

        //correo
        if (!email.isNullOrBlank()) {
            when {
                email.length < 5 -> errors["email"] = "Debe tener al menos 5 caracteres"
                email.length > 300 -> errors["email"] = "No puede exceder 300 caracteres"
                !emailRegex.matches(email) -> errors["email"] = "Formato de correo no válido"
            }
        }

        //dirección
        if (!address.isNullOrBlank()) {
            when {
                address.length < 2 -> errors["address"] = "La dirección es muy corta"
                address.length > 100 -> errors["address"] = "No debe exceder 100 caracteres"
                !addressRegex.matches(address) -> errors["address"] =
                    "Caracteres inválidos en la dirección"
            }
        }

        return errors
    }
}