package com.spendmindai.app.core.infrastructure.ai

import com.spendmindai.app.core.domain.model.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryMatchingService @Inject constructor() {

    /**
     * Attempts to find the best matching [Category] from [availableCategories] for the
     * supplied [categoryName] string.
     *
     * Matching strategy (in priority order):
     * 1. Exact match (case-insensitive).
     * 2. The category name *starts with* the query (or vice-versa).
     * 3. The category name *contains* the query (or vice-versa).
     * 4. Any individual word from the query matches a word inside the category name.
     *
     * Returns `null` when no reasonable match is found.
     */
    fun findBestMatch(
        categoryName: String,
        availableCategories: List<Category>
    ): Category? {
        if (categoryName.isBlank() || availableCategories.isEmpty()) return null

        val query = categoryName.trim().lowercase()

        // 1. Exact match
        availableCategories.firstOrNull { it.name.lowercase() == query }
            ?.let { return it }

        // 2. Starts-with match (query starts with category name or category name starts with query)
        availableCategories.firstOrNull { cat ->
            val catLower = cat.name.lowercase()
            catLower.startsWith(query) || query.startsWith(catLower)
        }?.let { return it }

        // 3. Contains match (category name contains query or query contains category name)
        availableCategories.firstOrNull { cat ->
            val catLower = cat.name.lowercase()
            catLower.contains(query) || query.contains(catLower)
        }?.let { return it }

        // 4. Word-level match – any query word appears in category name words
        val queryWords = query.split(Regex("\\s+")).filter { it.length > 2 }
        if (queryWords.isNotEmpty()) {
            availableCategories.firstOrNull { cat ->
                val catWords = cat.name.lowercase().split(Regex("\\s+"))
                queryWords.any { qw -> catWords.any { cw -> cw.contains(qw) || qw.contains(cw) } }
            }?.let { return it }
        }

        return null
    }

    /**
     * Scores similarity between [query] and [candidate] on a 0.0–1.0 scale.
     * Useful for ranked matching when a single best match is ambiguous.
     */
    fun similarityScore(query: String, candidate: String): Double {
        val q = query.lowercase().trim()
        val c = candidate.lowercase().trim()
        if (q == c) return 1.0
        if (q.isEmpty() || c.isEmpty()) return 0.0
        val longer = if (q.length > c.length) q else c
        val shorter = if (q.length <= c.length) q else c
        val editDistance = levenshtein(q, c)
        return (longer.length - editDistance).toDouble() / longer.length.toDouble()
    }

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) {
                    dp[i - 1][j - 1]
                } else {
                    1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
                }
            }
        }
        return dp[a.length][b.length]
    }
}
