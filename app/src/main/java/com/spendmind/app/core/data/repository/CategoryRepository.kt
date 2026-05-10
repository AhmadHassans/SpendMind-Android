package com.spendmindai.app.core.data.repository

import com.spendmindai.app.core.data.database.dao.CategoryDao
import com.spendmindai.app.core.data.database.entities.toEntity
import com.spendmindai.app.core.data.database.entities.toDomainModel
import com.spendmindai.app.core.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories().map { list ->
        list.map { it.toDomainModel() }
    }

    fun getEnabledCategories(): Flow<List<Category>> = categoryDao.getEnabledCategories().map { list ->
        list.map { it.toDomainModel() }
    }

    suspend fun getCategoryById(id: String): Category? =
        categoryDao.getCategoryById(id)?.toDomainModel()

    suspend fun getCategoryByName(name: String): Category? =
        categoryDao.getCategoryByName(name)?.toDomainModel()

    suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }

    suspend fun insertCategories(categories: List<Category>) {
        categoryDao.insertCategories(categories.map { it.toEntity() })
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toEntity())
    }

    suspend fun getAllCategoriesOnce(): List<Category> =
        categoryDao.getAllCategoriesOnce().map { it.toDomainModel() }

    suspend fun getCategoryCount(): Int =
        categoryDao.getCategoryCount()

    suspend fun seedDefaultCategories() {
        val count = categoryDao.getCategoryCount()
        if (count == 0) {
            categoryDao.insertCategories(Category.DEFAULT_CATEGORIES.map { it.toEntity() })
        }
    }

    suspend fun findOrCreateCategory(name: String): Category {
        val existing = categoryDao.getCategoryByName(name)
        if (existing != null) return existing.toDomainModel()

        val newCategory = Category(
            name = name,
            emoji = "📝",
            colorName = "gray",
            isCustom = true,
            order = getCategoryCount()
        )
        categoryDao.insertCategory(newCategory.toEntity())
        return newCategory
    }
}
