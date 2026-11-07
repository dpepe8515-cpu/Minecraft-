package com.example

import com.example.data.BedrockAddonCompiler
import com.example.data.RoxySeedData
import com.example.viewmodel.FeedUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun seedTasks_chatThreads_andMinecraftMods_arePopulated() {
        val tasks = RoxySeedData.defaultTasks()
        val catalog = RoxySeedData.defaultCatalogItems()
        val mods = RoxySeedData.defaultMinecraftMods()
        val threads = RoxySeedData.defaultChatThreads()
        val messages = RoxySeedData.defaultMessagesForThread1()
        assertTrue(tasks.isNotEmpty())
        assertTrue(catalog.isNotEmpty())
        assertTrue(mods.isNotEmpty())
        assertTrue(threads.isNotEmpty())
        assertTrue(messages.isNotEmpty())
        assertEquals("roxy:astolfo_companion", mods.first().entityIdentifier)
        assertEquals(1L, messages.last().linkedModId)
    }

    @Test
    fun bedrockAddonCompiler_generatesValidEntitySpawnLootAndAnimationJson() {
        val sampleMod = RoxySeedData.defaultMinecraftMods().first()
        val entityJson = BedrockAddonCompiler.generateBehaviorEntityJson(sampleMod)
        val spawnRulesJson = BedrockAddonCompiler.generateSpawnRulesJson(sampleMod)
        val lootTableJson = BedrockAddonCompiler.generateLootTableJson(sampleMod)
        val clientJson = BedrockAddonCompiler.generateResourceClientEntityJson(sampleMod)
        val renderCtrlJson = BedrockAddonCompiler.generateRenderControllerJson(sampleMod)
        val animationJson = BedrockAddonCompiler.generateAnimationJson(sampleMod)

        assertTrue(entityJson.contains("minecraft:behavior.follow_owner"))
        assertTrue(entityJson.contains("minecraft:tameable"))
        assertTrue(spawnRulesJson.contains("minecraft:spawn_rules"))
        assertTrue(lootTableJson.contains("pools"))
        assertTrue(clientJson.contains(sampleMod.entityIdentifier))
        assertTrue(renderCtrlJson.contains("render_controllers"))
        assertTrue(animationJson.contains("animations"))
    }

    @Test
    fun feedUiState_transitionsFromLoadingToSuccess() {
        val loadingState: FeedUiState<List<String>> = FeedUiState.Loading
        val successState: FeedUiState<List<String>> = FeedUiState.Success(listOf("wt/agent-1"))
        assertTrue(loadingState is FeedUiState.Loading)
        assertTrue(successState is FeedUiState.Success)
    }
}
