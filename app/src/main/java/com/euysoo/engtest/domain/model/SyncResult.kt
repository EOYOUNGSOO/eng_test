package com.euysoo.engtest.domain.model

data class SyncResult(
    val totalInFile: Int,
    val addedCount: Int,
    val updatedCount: Int,
    val skippedCount: Int,
    val sourceVersion: String,
)
