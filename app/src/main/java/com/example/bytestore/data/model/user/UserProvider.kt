package com.example.bytestore.data.model.user

object UserProvider {
    private var cachedUsers: ListUsersModel? = null

    //obtener usuarios
    fun getFetchUsers(): ListUsersModel? {
        return cachedUsers
    }

    //agregar usuario
    fun addUser(user: UserModel) {
        if (cachedUsers == null) throw IllegalStateException("La lista de chachedUsers es null")
        val currentList = cachedUsers!!.data

        //validar si ya existe
        val newList = if (currentList.any { it.id == user.id }) {
            //actulizar si existe
            currentList.map { users ->
                if (users.id == user.id) user else users
            }
        } else {
            //agregar si no existe
            currentList + user
        }

        cachedUsers = cachedUsers!!.copy(
            total = newList.size,
            data = newList
        )
    }

    //agregar datos
    fun addFetchUsers(listUsersModel: ListUsersModel) {
        if (cachedUsers != null) {
            val current = cachedUsers!!.data

            // Filtrar usuarios nuevos que no existan por id
            val newUsers = listUsersModel.data.filter { incoming ->
                current.none { it.id == incoming.id }
            }

            // Crear lista final
            val finalList = current + newUsers

            cachedUsers = cachedUsers!!.copy(
                total = finalList.size,
                data = finalList
            )
        } else {
            cachedUsers = listUsersModel
        }
    }

    //buscar usuario por id
    fun findUserById(id: String): UserModel? {
        return cachedUsers?.data?.find { it.id == id }
    }

    //buscar usuario
    fun findUser(search: String): ListUsersModel? {
        if (cachedUsers == null || cachedUsers?.total != cachedUsers?.data?.size) return null
        val terms = search.lowercase().split(" ").filter { it.isNotBlank() }
        val searchUser = cachedUsers!!.data.filter { user ->
            terms.any { term ->
                user.name.lowercase().contains(term) || user.email.lowercase().contains(term)
            }
        }.toMutableList()
        return ListUsersModel(
            total = searchUser.size,
            pages = 1,
            first = 1,
            next = null,
            prev = null,
            data = searchUser
        )
    }

    //remover usuario
    fun removeUser(id: String) {
        if (cachedUsers == null) throw IllegalStateException("La lista de chachedUsers es null")

        val current = cachedUsers!!.data

        //obtener lista sin el usuario
        val newUsers = current.filter {
            it.id != id
        }

        //si no existe el id
        if(newUsers.size == current.size) return

        cachedUsers = cachedUsers!!.copy(
            total = newUsers.size,
            data = newUsers
        )
    }
}