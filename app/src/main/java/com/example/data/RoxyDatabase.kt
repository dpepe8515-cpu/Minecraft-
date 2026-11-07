package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "agent_tasks")
data class AgentTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val repository: String,
    val worktreeBranch: String,
    val modelProvider: String,
    val status: String, // "RUNNING", "COMPLETED", "REVIEW", "ERROR"
    val progressPercent: Int,
    val tokensUsed: Int,
    val latencyMs: Int,
    val filesChanged: Int,
    val lastStepSummary: String,
    val isBookmarked: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "catalog_items")
data class CatalogItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String, // "MODEL", "MCP_SKILL", "TEMPLATE", "GITHUB_REPO"
    val provider: String,
    val badgeText: String,
    val metricLabel: String,
    val description: String,
    val endpointOrCommand: String,
    val isInstalled: Boolean = false,
    val starCount: Int = 0
)

@Entity(tableName = "agent_logs")
data class AgentLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val stepType: String, // "PLAN", "WORKTREE", "TOOL_CALL", "DIFF", "RESPONSE"
    val title: String,
    val detail: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "minecraft_mods")
data class MinecraftModEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val entityIdentifier: String,
    val modCategory: String, // "NPC_COMPANION", "WEAPON_ITEM", "MOB_BOSS", "WORLD_PACK"
    val bedrockVersion: String, // "1.21.70+", "1.21.50", "1.21.0", "1.20.80"
    val promptUsed: String,
    val maxHealth: Int,
    val moveSpeed: Double,
    val attackDamage: Int,
    val tameItem: String,
    val followsPlayer: Boolean,
    val defendsPlayer: Boolean,
    val dialogueLines: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_threads")
data class ChatThreadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val selectedModel: String = "RoxyGPT-4o • Mod Engine",
    val bedrockVersion: String = "1.21.70+",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: Long,
    val role: String, // "USER" or "ASSISTANT"
    val content: String,
    val linkedModId: Long? = null,
    val codeSnippet: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface RoxyDao {
    @Query("SELECT * FROM agent_tasks ORDER BY isBookmarked DESC, createdAt DESC")
    fun observeAllTasks(): Flow<List<AgentTaskEntity>>

    @Query("SELECT * FROM agent_tasks ORDER BY isBookmarked DESC, createdAt DESC")
    suspend fun getAllTasksOnce(): List<AgentTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: AgentTaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<AgentTaskEntity>)

    @Update
    suspend fun updateTask(task: AgentTaskEntity)

    @Query("DELETE FROM agent_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long)

    @Query("SELECT * FROM catalog_items ORDER BY isInstalled DESC, starCount DESC")
    fun observeCatalogItems(): Flow<List<CatalogItemEntity>>

    @Query("SELECT * FROM catalog_items ORDER BY isInstalled DESC, starCount DESC")
    suspend fun getCatalogItemsOnce(): List<CatalogItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCatalogItems(items: List<CatalogItemEntity>)

    @Update
    suspend fun updateCatalogItem(item: CatalogItemEntity)

    @Query("SELECT * FROM agent_logs WHERE taskId = :taskId ORDER BY timestamp ASC")
    fun observeLogsForTask(taskId: Long): Flow<List<AgentLogEntity>>

    @Query("SELECT * FROM agent_logs WHERE taskId = :taskId ORDER BY timestamp ASC")
    suspend fun getLogsForTaskOnce(taskId: Long): List<AgentLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AgentLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<AgentLogEntity>)

    @Query("SELECT * FROM minecraft_mods ORDER BY createdAt DESC")
    fun observeAllMinecraftMods(): Flow<List<MinecraftModEntity>>

    @Query("SELECT * FROM minecraft_mods ORDER BY createdAt DESC")
    suspend fun getAllMinecraftModsOnce(): List<MinecraftModEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMinecraftMod(mod: MinecraftModEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMinecraftMods(mods: List<MinecraftModEntity>)

    @Update
    suspend fun updateMinecraftMod(mod: MinecraftModEntity)

    @Query("DELETE FROM minecraft_mods WHERE id = :modId")
    suspend fun deleteMinecraftMod(modId: Long)

    // Chat Threads & Messages
    @Query("SELECT * FROM chat_threads ORDER BY updatedAt DESC")
    fun observeChatThreads(): Flow<List<ChatThreadEntity>>

    @Query("SELECT * FROM chat_threads ORDER BY updatedAt DESC")
    suspend fun getChatThreadsOnce(): List<ChatThreadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatThread(thread: ChatThreadEntity): Long

    @Update
    suspend fun updateChatThread(thread: ChatThreadEntity)

    @Query("DELETE FROM chat_threads WHERE id = :threadId")
    suspend fun deleteChatThread(threadId: Long)

    @Query("SELECT * FROM chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun observeMessagesForThread(threadId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesForThread(threadId: Long)
}

@Database(
    entities = [
        AgentTaskEntity::class,
        CatalogItemEntity::class,
        AgentLogEntity::class,
        MinecraftModEntity::class,
        ChatThreadEntity::class,
        ChatMessageEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class RoxyDatabase : RoomDatabase() {
    abstract fun roxyDao(): RoxyDao

    companion object {
        @Volatile
        private var INSTANCE: RoxyDatabase? = null

        fun getInstance(context: Context): RoxyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoxyDatabase::class.java,
                    "nexus_roxy.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object RoxySeedData {
    fun defaultMinecraftMods(): List<MinecraftModEntity> = listOf(
        MinecraftModEntity(
            id = 1L,
            name = "Astolfo • Compañero NPC Leal",
            entityIdentifier = "roxy:astolfo_companion",
            modCategory = "NPC_COMPANION",
            bedrockVersion = "1.21.70+",
            promptUsed = "Compañero NPC estilo anime con estética pastel rosa/violeta que me siga a todos lados, acepte flores o diamantes, converse conmigo y me defienda de mobs hostiles en Minecraft Bedrock.",
            maxHealth = 80,
            moveSpeed = 0.34,
            attackDamage = 12,
            tameItem = "minecraft:poppy",
            followsPlayer = true,
            defendsPlayer = true,
            dialogueLines = "¡Qué alegría verte de nuevo! Iré contigo a donde vayas.\n¡Cuidado! Yo te protegeré de esos monstruos.\n¡Muchas gracias por el regalo, eres mi persona favorita en este mundo!",
            primaryColorHex = "#F472B6",
            secondaryColorHex = "#8B5CF6",
            createdAt = System.currentTimeMillis() - 60_000L
        ),
        MinecraftModEntity(
            id = 2L,
            name = "Roxy Valkyrie • Guardiana Aliada",
            entityIdentifier = "roxy:valkyrie_ally",
            modCategory = "NPC_COMPANION",
            bedrockVersion = "1.21.70+",
            promptUsed = "Aliada carismática y protectora que me acompañe en cuevas, tenga diálogos amigables, se domestique con amatistas o manzanas doradas y tenga regeneración de vida.",
            maxHealth = 120,
            moveSpeed = 0.36,
            attackDamage = 16,
            tameItem = "minecraft:amethyst_shard",
            followsPlayer = true,
            defendsPlayer = true,
            dialogueLines = "¡Siempre a tu lado! Ningún mob se acercará mientras esté aquí.\n¿Exploramos juntos la siguiente dimensión?\n¡Gracias por el detalle, me encanta pasar tiempo contigo!",
            primaryColorHex = "#00F0FF",
            secondaryColorHex = "#EC4899",
            createdAt = System.currentTimeMillis() - 180_000L
        ),
        MinecraftModEntity(
            id = 3L,
            name = "Katana Cyber-Netherite & Amuleto",
            entityIdentifier = "roxy:cyber_katana",
            modCategory = "WEAPON_ITEM",
            bedrockVersion = "1.21.50",
            promptUsed = "Espada mítica brillante con receta en mesa de crafteo, daño elevado y entidad guardiana invocada.",
            maxHealth = 60,
            moveSpeed = 0.32,
            attackDamage = 24,
            tameItem = "minecraft:diamond",
            followsPlayer = true,
            defendsPlayer = true,
            dialogueLines = "Hoja sincronizada al 100%.\nEnergía lista para el combate.",
            primaryColorHex = "#10B981",
            secondaryColorHex = "#090A0F",
            createdAt = System.currentTimeMillis() - 360_000L
        )
    )

    fun defaultChatThreads(): List<ChatThreadEntity> = listOf(
        ChatThreadEntity(
            id = 1L,
            title = "Compañero NPC Leal • Bedrock 1.21.70+",
            selectedModel = "RoxyGPT-4o • Mod Engine",
            bedrockVersion = "1.21.70+",
            updatedAt = System.currentTimeMillis() - 30_000L
        ),
        ChatThreadEntity(
            id = 2L,
            title = "Guardiana Aliada & Katana Mítica",
            selectedModel = "Claude 3.7 Sonnet • Roxy",
            bedrockVersion = "1.21.70+",
            updatedAt = System.currentTimeMillis() - 120_000L
        )
    )

    fun defaultMessagesForThread1(): List<ChatMessageEntity> = listOf(
        ChatMessageEntity(
            threadId = 1L,
            role = "USER",
            content = "Hazme un mod para Minecraft Bedrock en Android de un compañero NPC estilo anime muy leal y afectuoso que me siga a todos lados, acepte flores o diamantes y me defienda de enemigos.",
            linkedModId = null,
            timestamp = System.currentTimeMillis() - 55_000L
        ),
        ChatMessageEntity(
            threadId = 1L,
            role = "ASSISTANT",
            content = "¡Listo! He diseñado y compilado el paquete **.mcaddon** completo para **Minecraft Bedrock 1.21.70+ (y versiones anteriores)**.\n\nIncluye:\n• **Comportamiento IA (`follow_owner` + `tameable`)**: Te sigue automáticamente, abre puertas y te defiende de cualquier mob hostil.\n• **Interacción y Regalos**: Puedes darle amapolas (`minecraft:poppy`), diamantes o manzanas doradas y responderá con sus frases personalizadas.\n• **Skin PNG 64x64 + Huevo Generador**: Empaquetados en el Resource Pack.\n\nPuedes **guardar el archivo `.mcaddon`** o **abrirlo directamente en Minecraft** desde la tarjeta inferior:",
            linkedModId = 1L,
            codeSnippet = "\"minecraft:behavior.follow_owner\": { \"priority\": 2, \"speed_multiplier\": 1.15 }",
            timestamp = System.currentTimeMillis() - 50_000L
        )
    )

    fun defaultMessagesForThread2(): List<ChatMessageEntity> = listOf(
        ChatMessageEntity(
            threadId = 2L,
            role = "USER",
            content = "Crea una compañera guardiana carismática que explore cuevas conmigo y tenga mucha vida.",
            linkedModId = null,
            timestamp = System.currentTimeMillis() - 130_000L
        ),
        ChatMessageEntity(
            threadId = 2L,
            role = "ASSISTANT",
            content = "He generado a **Roxy Valkyrie • Guardiana Aliada** (`roxy:valkyrie_ally`) con 120 HP, ataque cuerpo a cuerpo mejorado, domesticación con fragmentos de amatista y diálogos personalizados. Aquí tienes su tarjeta lista para exportar:",
            linkedModId = 2L,
            codeSnippet = "\"minecraft:health\": { \"value\": 120, \"max\": 120 }",
            timestamp = System.currentTimeMillis() - 125_000L
        )
    )

    fun defaultTasks(): List<AgentTaskEntity> = listOf(
        AgentTaskEntity(
            id = 1L,
            title = "Compilar Add-on .mcaddon de Compañero NPC para Bedrock 1.21.70",
            repository = "roxy-gg/mc-bedrock-studio",
            worktreeBranch = "wt/bedrock-npc-addon",
            modelProvider = "roxy-v1 / claude-3.7-sonnet",
            status = "RUNNING",
            progressPercent = 88,
            tokensUsed = 19240,
            latencyMs = 265,
            filesChanged = 7,
            lastStepSummary = "Generados manifest.json (BP/RP), comportamiento follow_owner y skin 64x64 PNG.",
            isBookmarked = true,
            createdAt = System.currentTimeMillis() - 120_000L
        ),
        AgentTaskEntity(
            id = 2L,
            title = "Refactorizar flujo OAuth2 y sesión remota QR",
            repository = "roxy-gg/roxy",
            worktreeBranch = "wt/agent-oauth-qr",
            modelProvider = "roxy-v1 / claude-3.7-sonnet",
            status = "RUNNING",
            progressPercent = 74,
            tokensUsed = 18420,
            latencyMs = 310,
            filesChanged = 6,
            lastStepSummary = "Ejecutando diagnósticos LSP en src/main/sessionBridge.ts sin conflictos de árbol.",
            isBookmarked = true,
            createdAt = System.currentTimeMillis() - 180_000L
        ),
        AgentTaskEntity(
            id = 3L,
            title = "Reemplazar spinners por Skeleton Shimmer en feeds",
            repository = "roxy-gg/roxy-android",
            worktreeBranch = "wt/skeleton-feed-ui",
            modelProvider = "roxy-v1 / gemini-2.5-pro",
            status = "COMPLETED",
            progressPercent = 100,
            tokensUsed = 12950,
            latencyMs = 245,
            filesChanged = 4,
            lastStepSummary = "Verificación completada: 0 desplazamientos de layout (CLS) y 60fps estables.",
            isBookmarked = true,
            createdAt = System.currentTimeMillis() - 950_000L
        )
    )

    fun defaultLogsForTask(taskId: Long): List<AgentLogEntity> = listOf(
        AgentLogEntity(
            taskId = taskId,
            stepType = "WORKTREE",
            title = "Worktree aislado inicializado",
            detail = "git worktree add ../roxy-wt-$taskId HEAD --detach (Copia aislada lista en 140ms)"
        ),
        AgentLogEntity(
            taskId = taskId,
            stepType = "PLAN",
            title = "Planificación de subagentes completada",
            detail = "Asignados 2 subagentes de análisis estático y 1 compilador de esquemas Bedrock."
        ),
        AgentLogEntity(
            taskId = taskId,
            stepType = "TOOL_CALL",
            title = "MCP Tool: bedrock_schema_validator & lsp_diagnostics",
            detail = "Esquemas JSON de Behavior Pack y Resource Pack validados para Minecraft Bedrock 1.20–1.21.70+."
        ),
        AgentLogEntity(
            taskId = taskId,
            stepType = "DIFF",
            title = "Parche aplicado en árbol de trabajo",
            detail = "+142 líneas / -38 líneas aplicadas limpiamente. Listo para exportar."
        )
    )

    fun defaultCatalogItems(): List<CatalogItemEntity> = listOf(
        CatalogItemEntity(
            id = "mdl-roxy-sonnet",
            title = "Claude 3.7 Sonnet (Roxy Gateway)",
            category = "MODEL",
            provider = "Roxy.gg / Anthropic",
            badgeText = "200K CTX",
            metricLabel = "48 tok/s • 290ms TTFT",
            description = "Modelo principal optimizado en Roxy.gg para agentes paralelos, edición multi-archivo y creación de mods Bedrock.",
            endpointOrCommand = "https://roxy.gg/v1/chat/completions",
            isInstalled = true,
            starCount = 1480
        ),
        CatalogItemEntity(
            id = "mcp-bedrock-compiler",
            title = "Bedrock Add-On & Skin Generator MCP",
            category = "MCP_SKILL",
            provider = "roxy-gg/mc-bedrock",
            badgeText = "MC 1.21.70+",
            metricLabel = "BP + RP + .mcaddon",
            description = "Genera entidades NPC, comportamientos follow_owner/tameable, diálogos, recetas y texturas PNG de 64x64.",
            endpointOrCommand = "roxy mcp start bedrock-addon-builder",
            isInstalled = true,
            starCount = 1690
        ),
        CatalogItemEntity(
            id = "mcp-sirup-connector",
            title = "Sirup MCP Skills Connector",
            category = "MCP_SKILL",
            provider = "roxy-gg/sirup",
            badgeText = "MCP STDIO/SSE",
            metricLabel = "14 Tools • Zero-Config",
            description = "Conector oficial open-source de Roxy.gg para enlazar servidores Model Context Protocol y skills locales.",
            endpointOrCommand = "npx @roxy-gg/sirup start --port 4120",
            isInstalled = true,
            starCount = 940
        ),
        CatalogItemEntity(
            id = "tpl-npc-companion",
            title = "Plantilla: Compañero NPC Personalizado (Bedrock)",
            category = "TEMPLATE",
            provider = "Roxy Mod Preset",
            badgeText = ".MCADDON",
            metricLabel = "IA Seguidor + Diálogos",
            description = "Crea cualquier compañero o aliada en Minecraft Bedrock con seguimiento automático, defensa y regalos.",
            endpointOrCommand = "roxy mod create --type npc_companion",
            isInstalled = true,
            starCount = 1240
        ),
        CatalogItemEntity(
            id = "repo-roxy-desktop",
            title = "roxy-gg/roxy",
            category = "GITHUB_REPO",
            provider = "GitHub Official",
            badgeText = "TypeScript • Electron",
            metricLabel = "Agente IDE Open-Source",
            description = "Repositorio principal de Roxy.gg: entorno de desarrollo con agentes paralelos y API compatible con OpenAI.",
            endpointOrCommand = "https://github.com/roxy-gg/roxy",
            isInstalled = true,
            starCount = 2890
        )
    )
}
