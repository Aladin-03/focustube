package dev.aladins.focustube.domain.model

/** A country, as its ISO 3166-1 alpha-2 code (the United Kingdom is "GB"). */
@JvmInline
value class Region(val code: String) {
    init {
        require(code.length == 2 && code.all { it in 'A'..'Z' }) { "Region code must be two capital letters: $code" }
    }

    companion object {
        /** Default regions for new personas (REQUIREMENTS.md "Regions"). */
        val Defaults: List<Region> = listOf("US", "GB", "DE", "FR", "ES", "IT", "NL").map(::Region)
    }
}
