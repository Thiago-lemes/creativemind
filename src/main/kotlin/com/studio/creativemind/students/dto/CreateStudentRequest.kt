package com.studio.creativemind.students.dto

import java.time.LocalDate

data class CreateStudentRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val birthDate: LocalDate? = null
)