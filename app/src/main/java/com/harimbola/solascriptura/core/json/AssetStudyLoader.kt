package com.harimbola.solascriptura.core.json

import android.content.Context
import com.harimbola.solascriptura.core.common.AppConstants

data class LoadedAssetStudy(
    val fileName: String,
    val categoryFolder: String,
    val rawJson: String,
    val document: StudyDocumentDto,
    val checksum: String
)

class AssetStudyLoader(private val context: Context) {

    fun loadAllValidBundledStudies(): List<LoadedAssetStudy> {
        val loaded = mutableListOf<LoadedAssetStudy>()
        val assetManager = context.assets

        for (categoryFolder in AppConstants.STUDY_CATEGORIES_ORDER) {
            val folderPath = "${AppConstants.ASSET_STUDIES_ROOT}/$categoryFolder"
            val files = try {
                assetManager.list(folderPath)?.sorted() ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }

            for (fileName in files) {
                if (!fileName.endsWith(".json", ignoreCase = true)) continue
                val fullPath = "$folderPath/$fileName"
                val rawJson = try {
                    assetManager.open(fullPath).bufferedReader(Charsets.UTF_8).use { it.readText() }
                } catch (_: Exception) {
                    continue
                }
                when (val result = DialogueValidator.parseAndValidate(rawJson)) {
                    is ValidationResult.Valid -> {
                        loaded.add(
                            LoadedAssetStudy(
                                fileName = fileName,
                                categoryFolder = categoryFolder,
                                rawJson = rawJson,
                                document = result.document,
                                checksum = result.checksum
                            )
                        )
                    }
                    is ValidationResult.Invalid -> {
                        // Invalid bundled JSON is safely skipped and never stored as valid
                    }
                }
            }
        }
        return loaded
    }
}
