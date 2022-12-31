package com.syndicg5.networking.models

import java.util.Date

data class Monument(
    val id: Int,
    val nom: String,
    val description: String,
    val adresse: String,
    val latitude: Double,
    val longitude: Double,
    val images: List<Images>,
    val createur: Createur,
    val ville: Ville,
)

data class Createur(
    val id: Int,
    val nom: String,
    val prenom: String,
    val description: String,
    val debut: Date,
    val fin: Date
)

data class Ville(
    val id: Int,
    val nom: String
)

data class Images(
    val id: Int,
    val url: String
)
