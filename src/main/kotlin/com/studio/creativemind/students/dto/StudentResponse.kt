package com.studio.creativemind.students.dto

import java.time.LocalDate
import java.util.UUID

data class StudentResponse(
    val id: UUID? = null,
    val name: String,
    val email: String,
    val phone: String? = null,
    val birthDate: LocalDate? = null,
    val active: Boolean = true
)

