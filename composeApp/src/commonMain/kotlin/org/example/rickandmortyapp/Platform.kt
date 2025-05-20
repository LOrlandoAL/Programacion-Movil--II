package org.example.rickandmortyapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform