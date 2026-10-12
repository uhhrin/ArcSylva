# 🌲 ArcSylva - Smart Natural Tree Felling Plugin

**Production-Grade Design Document (Paper 1.21.x)**

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Project Structure](#project-structure)
4. [Core Systems](#core-systems)
5. [Implementation Details](#implementation-details)
6. [Configuration](#configuration)
7. [Performance & Safety](#performance--safety)
8. [Testing Strategy](#testing-strategy)

---

## Overview

### Goals

- **Safety First**: Never break player-built structures
- **Performance**: Zero lag impact on servers
- **Compatibility**: Works with all major protection plugins
- **User Experience**: Intuitive, predictable behavior
- **Maintainability**: Clean code, SOLID principles

### Core Principle

> **Safe by default, powerful by choice**

Automatic tree felling is conservative. Players explicitly signal overrides through sneaking or confirmations.

---

## Architecture

### Design Patterns

- **Dependency Injection**: Constructor-based DI throughout
- **Strategy Pattern**: Tree detection algorithms
- **Observer Pattern**: Event-driven block tracking
- **Factory Pattern**: Tree scanner creation
- **Command Pattern**: Confirmation actions
- **Repository Pattern**: Data persistence abstraction

### SOLID Principles Application

- **Single Responsibility**: Each class has one clear purpose
- **Open/Closed**: Extensible via interfaces, closed for modification
- **Liskov Substitution**: All implementations are substitutable
- **Interface Segregation**: Focused, minimal interfaces
- **Dependency Inversion**: Depend on abstractions, not concretions

---

## Project Structure

```
src/main/java/com/yourname/arcsylva/
├── ArcSylvaPlugin.java                 # Main plugin class
│
├── api/                                 # Public API for other plugins
│   ├── event/
│   │   ├── TreeFellEvent.java
│   │   └── TreeDetectionEvent.java
│   ├── service/
│   │   ├── TreeService.java
│   │   └── ProtectionService.java
│   └── model/
│       └── TreeData.java
│
├── core/                                # Core business logic
│   ├── tree/
│   │   ├── TreeDetector.java           # Interface
│   │   ├── NaturalTreeDetector.java    # Implementation
│   │   ├── TreeScanner.java            # Interface
│   │   └── VerticalTreeScanner.java    # Implementation
│   ├── validation/
│   │   ├── TreeValidator.java          # Interface
│   │   ├── ValidationChain.java        # Chain of Responsibility
│   │   ├── PermissionValidator.java
│   │   ├── ToolValidator.java
│   │   ├── SneakValidator.java
│   │   └── NaturalBlockValidator.java
│   └── breaking/
│       ├── TreeBreaker.java            # Interface
│       ├── BatchTreeBreaker.java       # Implementation
│       └── BreakStrategy.java          # Interface
│
├── data/                                # Data persistence layer
│   ├── repository/
│   │   ├── BlockDataRepository.java    # Interface
│   │   └── PDCBlockRepository.java     # PDC implementation
│   ├── cache/
│   │   ├── BlockCache.java
│   │   └── ChunkCache.java
│   └── model/
│       ├── TrackedBlock.java
│       └── BlockOrigin.java            # Enum: NATURAL, PLAYER_PLACED
│
├── listener/                            # Event listeners
│   ├── BlockPlaceListener.java
│   ├── TreeBreakListener.java
│   ├── PlayerQuitListener.java
│   └── ChunkUnloadListener.java
│
├── confirmation/                        # Confirmation system
│   ├── ConfirmationManager.java        # Interface
│   ├── SimpleConfirmationManager.java  # Implementation
│   ├── ConfirmationRequest.java        # Value object
│   └── ConfirmationHandler.java        # Callback interface
│
├── config/                              # Configuration management
│   ├── ConfigManager.java              # Interface
│   ├── YamlConfigManager.java          # Implementation
│   ├── Settings.java                   # Immutable settings object
│   └── ConfigValidator.java
│
├── protection/                          # Protection plugin integration
│   ├── ProtectionBridge.java           # Interface
│   ├── ProtectionRegistry.java
│   └── impl/
│       ├── WorldGuardBridge.java
│       ├── GriefPreventionBridge.java
│       └── TownyBridge.java
│
├── permission/                          # Permission handling
│   ├── PermissionManager.java
│   └── PermissionNode.java             # Enum
│
├── task/                                # Async/scheduled tasks
│   ├── TreeBreakTask.java              # BukkitRunnable
│   └── ConfirmationCleanupTask.java
│
├── util/                                # Utilities
│   ├── MaterialSets.java               # Cached EnumSets
│   ├── BlockUtil.java
│   ├── MessageUtil.java
│   ├── LocationUtil.java
│   └── VersionUtil.java                # MC version compatibility
│
├── command/                             # Commands
│   ├── ArcSylvaCommand.java            # Main command
│   └── subcommand/
│       ├── ReloadCommand.java
│       ├── InfoCommand.java
│       └── ToggleCommand.java
│
└── metrics/                             # Statistics & monitoring
    ├── MetricsCollector.java
    └── PerformanceMonitor.java

src/main/resources/
├── plugin.yml
├── config.yml
├── messages.yml
└── paper-plugin.yml                     # Paper-specific config
```

---

## Core Systems

### 1. Tree Detection System

#### Interface Design

```java
public interface TreeDetector {
    /**
     * Detects if the given block is part of a natural tree.
     *
     * @param block The block to check
     * @param player The player breaking the block
     * @return Optional containing TreeData if valid tree, empty otherwise
     */
    Optional<TreeData> detectTree(Block block, Player player);
}
```

#### Implementation: NaturalTreeDetector

**Responsibilities**:
- Validate block is a log
- Check minimum vertical logs requirement
- Verify natural leaves in radius
- Detect player modifications

**Algorithm**:
```
1. Quick rejection checks (material, permissions, sneak state)
2. Scan vertically upward (bounded by max-blocks)
3. Check for natural leaves in configurable radius
4. Track encountered player-placed blocks
5. Return TreeData with metadata
```

**Performance**:
- Early exit on first failure
- Bounded iteration (no recursion)
- Cache material sets
- Reuse data structures

---

### 2. Validation Chain

#### Chain of Responsibility Pattern

```java
public interface TreeValidator {
    ValidationResult validate(Player player, Block block, TreeData tree);
}

public class ValidationChain {
    private final List<TreeValidator> validators;
    
    public ValidationResult validate(Player player, Block block, TreeData tree) {
        for (TreeValidator validator : validators) {
            ValidationResult result = validator.validate(player, block, tree);
            if (!result.isValid()) {
                return result;
            }
        }
        return ValidationResult.success();
    }
}
```

#### Validators

1. **PermissionValidator**: Checks `arcsylva.use` permission
2. **ToolValidator**: Validates player holds an axe (if required)
3. **SneakValidator**: Handles sneak-to-disable logic
4. **NaturalBlockValidator**: Ensures root block is natural
5. **ProtectionValidator**: Checks protection plugin compatibility

**Benefits**:
- Easy to add/remove validators
- Clear separation of concerns
- Testable in isolation

---

### 3. Data Persistence Layer

#### Repository Pattern

```java
public interface BlockDataRepository {
    void markAsPlayerPlaced(Block block);
    boolean isPlayerPlaced(Block block);
    void removeTracking(Block block);
    void clearChunk(Chunk chunk);
}
```

#### PDC Implementation

**Storage Strategy**:
- Store data in chunk PDC, not individual blocks
- Use compact bit-packed format
- Key: `arcsylva:placed_blocks`
- Value: Serialized set of relative block positions

**Format**:
```
ChunkPDC[arcsylva:placed_blocks] = [
    "x:y:z", // Relative coordinates within chunk
    "x:y:z",
    ...
]
```

**Memory Optimization**:
- Store only relative positions (0-15 for x/z, 0-319 for y)
- Compress using bit packing: `(x << 12) | (z << 8) | y`
- Result: 2 bytes per tracked block
- Typical tree: 20-40 bytes total

---

### 4. Tree Breaking System

#### Interface

```java
public interface TreeBreaker {
    /**
     * Breaks the tree in batches to prevent lag.
     *
     * @param tree The tree data
     * @param player The player who initiated the break
     * @param tool The tool used for breaking
     * @return CompletableFuture that completes when breaking finishes
     */
    CompletableFuture<BreakResult> breakTree(TreeData tree, Player player, ItemStack tool);
}
```

#### Batch Breaking Strategy

**Implementation Details**:
1. Schedule a repeating task (every tick)
2. Break N blocks per tick (configurable)
3. Fire `BlockBreakEvent` for each block
4. Respect event cancellation
5. Drop items naturally
6. Update player stats
7. Apply tool durability damage

**Safety Measures**:
- Validate player is online each tick
- Check player is in same world
- Stop if player moves too far
- Timeout after max duration

---

### 5. Confirmation System

#### Design

```java
public interface ConfirmationManager {
    void requestConfirmation(Player player, ConfirmationRequest request);
    void handleResponse(Player player, boolean accepted);
    void cancelAll(Player player);
}

public class ConfirmationRequest {
    private final UUID playerId;
    private final Location blockLocation;
    private final TreeData treeData;
    private final long expirationTime;
    private final ConfirmationHandler handler;
}
```

#### Interactive Messages

Use **Adventure API** for modern text components:
```java
Component message = Component.text()
    .append(Component.text("⚠ ", NamedTextColor.YELLOW))
    .append(Component.text("This tree appears modified. "))
    .append(Component.text("[FELL ANYWAY]")
        .color(NamedTextColor.RED)
        .decorate(TextDecoration.BOLD)
        .clickEvent(ClickEvent.runCommand("/arcsylva confirm yes")))
    .append(Component.text(" "))
    .append(Component.text("[CANCEL]")
        .color(NamedTextColor.GRAY)
        .clickEvent(ClickEvent.runCommand("/arcsylva confirm no")))
    .build();
```

**Timeout Handling**:
- Scheduled cleanup task runs every second
- Remove expired confirmations
- Notify player on expiration

---

### 6. Protection Plugin Integration

#### Bridge Pattern

```java
public interface ProtectionBridge {
    String getName();
    boolean canBreak(Player player, Block block);
    int getPriority(); // For conflict resolution
}
```

#### Registry

```java
public class ProtectionRegistry {
    private final List<ProtectionBridge> bridges = new ArrayList<>();
    
    public void register(ProtectionBridge bridge) {
        bridges.add(bridge);
        bridges.sort(Comparator.comparing(ProtectionBridge::getPriority));
    }
    
    public boolean canBreakBlock(Player player, Block block) {
        for (ProtectionBridge bridge : bridges) {
            if (!bridge.canBreak(player, block)) {
                return false;
            }
        }
        return true;
    }
}
```

**Supported Plugins**:
- WorldGuard
- GriefPrevention
- Towny
- Lands
- Residence
- (Extensible via API)

---

## Implementation Details

### Configuration Management

#### Immutable Settings Pattern

```java
public final class Settings {
    private final boolean requireAxe;
    private final boolean sneakToDisable;
    private final boolean confirmModified;
    private final int maxBlocks;
    private final int blocksPerTick;
    private final int leafRadius;
    private final int minVerticalLogs;
    private final boolean replantSapling;
    private final Set<String> enabledWorlds;
    
    // Builder pattern for construction
    public static class Builder { ... }
}
```

**Benefits**:
- Thread-safe by default
- Easy to validate
- Hot-reload support
- No setter confusion

#### YAML Structure

```yaml
# Tool & Input Settings
require-axe: true
sneak-to-disable: true

# Safety Settings
confirm-modified-trees: true
respect-protection-plugins: true

# Performance Limits
limits:
  max-blocks-per-tree: 64
  blocks-per-tick: 12
  scan-timeout-ms: 100

# Tree Detection
detection:
  min-vertical-logs: 2
  leaf-search-radius: 4
  allowed-log-types:
    - OAK_LOG
    - BIRCH_LOG
    - SPRUCE_LOG
    - # ... etc

# Features
features:
  replant-sapling: true
  drop-saplings: true
  apply-fortune: true

# Worlds
enabled-worlds:
  - world
  - world_nether

# Messages (or reference messages.yml)
messages:
  tree-felled: "&aTree felled! &7({blocks} blocks)"
  modified-tree-warning: "&e⚠ This tree appears modified."
  # ... etc
```

---

### Permission Structure

```
arcsylva.use              # Basic tree felling
arcsylva.force            # Bypass modification checks with sneak
arcsylva.confirm          # Receive confirmation prompts
arcsylva.bypass.limit     # Ignore max-blocks limit
arcsylva.admin.reload     # Reload configuration
arcsylva.admin.info       # View debug information

# Dynamic limits (checked in order)
arcsylva.limit.512        # Can fell trees up to 512 blocks
arcsylva.limit.256
arcsylva.limit.128
arcsylva.limit.64
arcsylva.limit.32
```

---

### Event Handling Best Practices

#### Priority & Cancellation

```java
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onBlockBreak(BlockBreakEvent event) {
    // MONITOR = run last, after all protection plugins
    // ignoreCancelled = respect protection plugin decisions
}
```

#### Event Flow

```
1. Player breaks log block
   ↓
2. Protection plugins check (NORMAL priority)
   ↓
3. ArcSylva listener fires (MONITOR priority)
   ↓
4. If event already cancelled → do nothing
   ↓
5. Otherwise, check if tree qualifies
   ↓
6. If yes → cancel event, start tree breaking
   ↓
7. Fire synthetic events for each block
```

---

### Error Handling Strategy

#### Defensive Programming

```java
public class TreeBreaker {
    public CompletableFuture<BreakResult> breakTree(TreeData tree, Player player, ItemStack tool) {
        CompletableFuture<BreakResult> future = new CompletableFuture<>();
        
        try {
            // Validate inputs
            Objects.requireNonNull(tree, "tree cannot be null");
            Objects.requireNonNull(player, "player cannot be null");
            
            // Check preconditions
            if (!player.isOnline()) {
                future.completeExceptionally(new IllegalStateException("Player is offline"));
                return future;
            }
            
            // Execute safely
            scheduleSafeTask(tree, player, tool, future);
            
        } catch (Exception e) {
            logger.error("Failed to break tree", e);
            future.completeExceptionally(e);
        }
        
        return future;
    }
}
```

#### Logging Strategy

```java
// Use SLF4J with Paper
private static final Logger logger = LoggerFactory.getLogger(ArcSylvaPlugin.class);

// Log levels
logger.trace("Scanning block at {}", location);  // Verbose debugging
logger.debug("Tree detected with {} blocks", count); // Standard debugging
logger.info("ArcSylva enabled");                // Important state changes
logger.warn("Invalid configuration value: {}", key); // Recoverable issues
logger.error("Failed to load data", exception); // Serious errors
```

---

### Performance Optimizations

#### 1. Material Set Caching

```java
public final class MaterialSets {
    // Initialized once, reused forever
    public static final EnumSet<Material> LOGS = EnumSet.of(
        Material.OAK_LOG,
        Material.BIRCH_LOG,
        // ...
    );
    
    public static final EnumSet<Material> LEAVES = EnumSet.of(
        Material.OAK_LEAVES,
        Material.BIRCH_LEAVES,
        // ...
    );
    
    public static final EnumSet<Material> AXES = EnumSet.of(
        Material.WOODEN_AXE,
        Material.STONE_AXE,
        // ...
    );
    
    private MaterialSets() {} // Prevent instantiation
}
```

#### 2. Object Pooling

```java
public class TreeScannerPool {
    private final Queue<TreeScanner> pool = new ConcurrentLinkedQueue<>();
    private final int maxSize;
    
    public TreeScanner acquire() {
        TreeScanner scanner = pool.poll();
        return scanner != null ? scanner : new VerticalTreeScanner();
    }
    
    public void release(TreeScanner scanner) {
        scanner.reset();
        if (pool.size() < maxSize) {
            pool.offer(scanner);
        }
    }
}
```

#### 3. Lazy Initialization

```java
public class ConfigManager {
    private volatile Settings settings; // Cache
    private final Object lock = new Object();
    
    public Settings getSettings() {
        if (settings == null) {
            synchronized (lock) {
                if (settings == null) {
                    settings = loadSettings();
                }
            }
        }
        return settings;
    }
}
```

#### 4. Batch Operations

```java
// BAD: Fire event for each block individually in same tick
for (Block block : blocks) {
    breakBlock(block);
}

// GOOD: Spread over multiple ticks
new BukkitRunnable() {
    int index = 0;
    
    @Override
    public void run() {
        int batchSize = settings.getBlocksPerTick();
        for (int i = 0; i < batchSize && index < blocks.size(); i++, index++) {
            breakBlock(blocks.get(index));
        }
        
        if (index >= blocks.size()) {
            cancel();
        }
    }
}.runTaskTimer(plugin, 0L, 1L);
```

---

### Thread Safety

#### Bukkit Thread Rules

```java
// NEVER access Bukkit API from async threads
// BAD:
CompletableFuture.runAsync(() -> {
    block.setType(Material.AIR); // CRASH!
});

// GOOD:
CompletableFuture.runAsync(() -> {
    // Do expensive computation
    List<Block> blocks = expensiveCalculation();
    return blocks;
}).thenAcceptAsync(blocks -> {
    // Return to main thread for Bukkit API
    Bukkit.getScheduler().runTask(plugin, () -> {
        for (Block block : blocks) {
            block.setType(Material.AIR);
        }
    });
});
```

#### Concurrent Collections

```java
// For data accessed by multiple threads
private final Map<UUID, ConfirmationRequest> confirmations = new ConcurrentHashMap<>();

// For single-threaded access (main thread only)
private final Map<Location, TreeData> treeCache = new HashMap<>();
```

---

## Configuration

### Complete config.yml

```yaml
#  █████╗ ██████╗  ██████╗███████╗██╗   ██╗██╗    ██╗   ██╗ █████╗ 
# ██╔══██╗██╔══██╗██╔════╝██╔════╝╚██╗ ██╔╝██║    ██║   ██║██╔══██╗
# ███████║██████╔╝██║     ███████╗ ╚████╔╝ ██║    ██║   ██║███████║
# ██╔══██║██╔══██╗██║     ╚════██║  ╚██╔╝  ██║    ╚██╗ ██╔╝██╔══██║
# ██║  ██║██║  ██║╚██████╗███████║   ██║   ███████╗╚████╔╝ ██║  ██║
# ╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝╚══════╝   ╚═╝   ╚══════╝ ╚═══╝  ╚═╝  ╚═╝
# Smart Natural Tree Felling v1.0.0

# ═══════════════════════════════════════════════════════════════
# GENERAL SETTINGS
# ═══════════════════════════════════════════════════════════════

# Require an axe to fell trees automatically?
require-axe: true

# Allow players to disable tree felling by sneaking?
sneak-to-disable: true

# Ask for confirmation when breaking modified trees?
# (Trees with player-placed logs mixed with natural logs)
confirm-modified-trees: true

# Respect protection plugins (WorldGuard, GriefPrevention, etc.)?
# Recommended: true
respect-protection-plugins: true

# ═══════════════════════════════════════════════════════════════
# PERFORMANCE LIMITS
# ═══════════════════════════════════════════════════════════════

limits:
  # Maximum blocks in a single tree (prevents abuse)
  max-blocks-per-tree: 64
  
  # Blocks broken per tick (prevents lag)
  # Higher = faster but more laggy
  # Recommended: 8-16 for normal servers, 4-8 for busy servers
  blocks-per-tick: 12
  
  # Maximum time to spend scanning a tree (milliseconds)
  # Prevents lag from massive structures
  scan-timeout-ms: 100
  
  # Maximum trees that can be queued for breaking per player
  max-queued-trees: 3

# ═══════════════════════════════════════════════════════════════
# TREE DETECTION
# ═══════════════════════════════════════════════════════════════

detection:
  # Minimum connected logs going upward to qualify as a tree
  # Prevents log pillars/towers from being auto-felled
  min-vertical-logs: 2
  
  # Radius to search for leaves (blocks)
  # Larger = more lenient, but slower
  leaf-search-radius: 4
  
  # Minimum natural leaves required to qualify as tree
  min-natural-leaves: 1
  
  # Types of logs that count as trees
  # Remove any you don't want to be auto-felled
  allowed-log-types:
    - OAK_LOG
    - BIRCH_LOG
    - SPRUCE_LOG
    - JUNGLE_LOG
    - ACACIA_LOG
    - DARK_OAK_LOG
    - MANGROVE_LOG
    - CHERRY_LOG
    - CRIMSON_STEM
    - WARPED_STEM

# ═══════════════════════════════════════════════════════════════
# FEATURES
# ═══════════════════════════════════════════════════════════════

features:
  # Automatically replant a sapling where the tree was cut?
  replant-sapling: true
  
  # Should felled trees drop saplings?
  drop-saplings: true
  
  # Apply fortune enchantment to drops?
  apply-fortune: true
  
  # Apply tool durability damage?
  # If false, tool takes no damage from tree felling
  apply-durability: true
  
  # Damage per block broken (set to 0 for no damage)
  # 1 = normal damage, 0.5 = half damage, etc.
  durability-multiplier: 1.0
  
  # Play sound when tree is felled?
  play-sound: true
  sound-type: BLOCK_WOOD_BREAK
  
  # Spawn particles when tree is felled?
  spawn-particles: true
  particle-type: BLOCK_CRACK

# ═══════════════════════════════════════════════════════════════
# CONFIRMATIONS
# ═══════════════════════════════════════════════════════════════

confirmations:
  # How long confirmations last (seconds)
  timeout: 8
  
  # Cancel confirmation if player moves too far?
  cancel-on-move: true
  
  # Maximum distance player can move (blocks)
  max-move-distance: 4

# ═══════════════════════════════════════════════════════════════
# WORLDS
# ═══════════════════════════════════════════════════════════════

# List of worlds where ArcSylva is enabled
# Leave empty to enable in all worlds
enabled-worlds:
  - world
  - world_nether
  - world_the_end

# ═══════════════════════════════════════════════════════════════
# ADVANCED
# ═══════════════════════════════════════════════════════════════

advanced:
  # Enable debug logging?
  debug: false
  
  # Collect performance metrics?
  # (View with /arcsylva metrics)
  metrics: true
  
  # Cache tree scan results? (reduces CPU but uses more memory)
  cache-scans: true
  
  # Cache expiration (seconds)
  cache-expiration: 30
  
  # Check for updates on startup?
  check-updates: true
```

---

## Performance & Safety

### Performance Targets

| Metric | Target | Measurement |
|--------|--------|-------------|
| Tree scan time | < 1ms | Average case |
| Tree scan time | < 5ms | Worst case (max blocks) |
| Memory per tracked block | 2 bytes | PDC storage |
| TPS impact | 0 | Idle state |
| TPS impact | < 0.1 | Active tree felling |
| Max concurrent trees | 50+ | Per server |

### Safety Guarantees

1. **Never break player-placed blocks automatically**
   - Tracked via PDC
   - Persists across restarts
   - Immutable once placed

2. **Never exceed max-blocks limit**
   - Hard-coded failsafe
   - Per-player permission overrides
   - Logged when limit hit

3. **Never bypass protection plugins**
   - Uses `ignoreCancelled = true`
   - Fires events for every block
   - Respects all protection hooks

4. **Never cause async world access**
   - All Bukkit API calls on main thread
   - Schedulers used correctly
   - Validated with thread assertions

5. **Never leak memory**
   - Weak references where appropriate
   - Cleanup on player quit
   - Cleanup on chunk unload
   - Scheduled GC-friendly cleanup tasks

### Edge Cases Handled

| Edge Case | Solution |
|-----------|----------|
| Log pillars (no leaves) | `min-vertical-logs` + leaf check |
| Leaf roofs | Require natural leaves |
| Custom player trees | Only fell if forced |
| Chunk borders | Stop scan at border |
| World borders | Boundary checks |
| Protection plugins | Event cancellation |
| Player logout mid-fell | Task cancellation |
| Server restart | PDC persistence |
| Huge jungle trees | Max-blocks + timeout |
| Nether trees (stems) | Material set configuration |
| Modified trees | Confirmation system |
| Concurrent breaks | Thread-safe collections |
| Tool breaks mid-fell | Graceful handling |

---

## Testing Strategy

### Unit Tests (JUnit 5 + Mockito)

```java
@ExtendWith(MockitoExtension.class)
class NaturalTreeDetectorTest {
    @Mock private Block mockBlock;
    @Mock private Player mockPlayer;
    
    private NaturalTreeDetector detector;
    
    @BeforeEach
    void setUp() {
        detector = new NaturalTreeDetector(mockSettings);
    }
    
    @Test
    void detectTree_WithNaturalOakTree_ReturnsTreeData() {
        // Arrange
        when(mockBlock.getType()).thenReturn(Material.OAK_LOG);
        // ... setup tree structure
        
        // Act
        Optional<TreeData> result = detector.detectTree(mockBlock, mockPlayer);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals(15, result.get().getBlockCount());
    }
    
    @Test
    void detectTree_WithPlayerPlacedLog_ReturnsEmpty() {
        // ... test implementation
    }
}
```

### Integration Tests (Paper MockBukkit)

```java
@ExtendWith(MockBukkitExtension.class)
class TreeBreakIntegrationTest {
    private ServerMock server;
    private ArcSylvaPlugin plugin;
    
    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(ArcSylvaPlugin.class);
    }
    
    @Test
    void breakingNaturalTree_FellsEntireTree() {
        // Create test world with natural tree
        // Simulate player breaking bottom log
        // Assert all blocks broken
        // Assert sapling replanted
    }
}
```

### Performance Tests (JMH)

```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class TreeDetectionBenchmark {
    @Benchmark
    public void detectSmallTree(Blackhole bh) {
        // Benchmark small tree (20 blocks)
    }
    
    @Benchmark
    public void detectLargeTree(Blackhole bh) {
        // Benchmark large tree (64 blocks)
    }
}
```

### Manual Testing Checklist

- [ ] Natural oak tree breaks correctly
- [ ] Player-built tree does NOT break
- [ ] Modified tree shows confirmation
- [ ] Sneak disables tree felling
- [ ] Wrong tool prevents felling
- [ ] Protection plugins respected
- [ ] Sapling replants correctly
- [ ] Fortune enchantment applies
- [ ] Tool durability decreases
- [ ] Confirmations expire
- [ ] Multiple players don't interfere
- [ ] Chunk unload doesn't break tracking
- [ ] Server restart preserves data
- [ ] Config reload works
- [ ] Permissions work correctly
- [ ] All tree types supported
- [ ] Nether stems work
- [ ] Edge of world doesn't crash
- [ ] Huge jungle trees respect limits

---

## Development Roadmap

### Phase 1: Foundation (Week 1)
- [ ] Project setup (Maven/Gradle)
- [ ] Plugin skeleton
- [ ] Configuration system
- [ ] Utility classes
- [ ] Material sets

### Phase 2: Core Logic (Week 2)
- [ ] Tree detection algorithm
- [ ] Validation chain
- [ ] PDC repository
- [ ] Event listeners

### Phase 3: Breaking System (Week 3)
- [ ] Tree breaker implementation
- [ ] Batch breaking task
- [ ] Drop handling
- [ ] Sapling replanting

### Phase 4: User Experience (Week 4)
- [ ] Confirmation system
- [ ] Commands
- [ ] Messages & localization
- [ ] Permissions

### Phase 5: Integration (Week 5)
- [ ] Protection plugin bridges
- [ ] API for other plugins
- [ ] Custom events

### Phase 6: Polish (Week 6)
- [ ] Performance optimization
- [ ] Error handling
- [ ] Logging
- [ ] Documentation

### Phase 7: Testing (Week 7)
- [ ] Unit tests
- [ ] Integration tests
- [ ] Performance benchmarks
- [ ] Manual testing

### Phase 8: Release (Week 8)
- [ ] Beta testing on live server
- [ ] Bug fixes
- [ ] Final optimization
- [ ] Public release

---

## Code Quality Standards

### Checkstyle Rules
- Max line length: 120
- Indentation: 4 spaces
- No wildcard imports
- Javadoc required for public API
- Consistent naming conventions

### PMD Rules
- Avoid deep nesting (max 3 levels)
- Cyclomatic complexity < 10
- No god classes (max 500 lines)
- No dead code
- Proper exception handling

### SpotBugs
- No null pointer dereferences
- No resource leaks
- No concurrency issues
- No security vulnerabilities

---

## Dependencies

### Required
```xml
<dependency>
    <groupId>io.papermc.paper</groupId>
    <artifactId>paper-api</artifactId>
    <version>1.21.4-R0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### Optional (Soft Dependencies)
```xml
<!-- Protection Plugins -->
<dependency>
    <groupId>com.sk89q.worldguard</groupId>
    <artifactId>worldguard-bukkit</artifactId>
    <version>7.0.9</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>com.griefprevention</groupId>
    <artifactId>griefprevention</artifactId>
    <version>16.18</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>com.palmergames.towny</groupId>
    <artifactId>towny</artifactId>
    <version>0.100.0.0</version>
    <scope>provided</scope>
</dependency>
```

### Build Tools
- **Maven** or **Gradle** (recommend Gradle for modern projects)
- **Java 21** (Paper 1.21.x requirement)
- **Paper-Mojmap** for mappings

### Development Tools
- **IntelliJ IDEA** (recommended)
- **Lombok** for boilerplate reduction
- **AnnotationProcessing** for compile-time validation

---

## Documentation Requirements

### Javadoc Coverage
- All public classes: 100%
- All public methods: 100%
- Complex private methods: As needed

### Example Javadoc
```java
/**
 * Detects whether a broken block is part of a natural tree and should trigger
 * automatic tree felling.
 * 
 * <p>Detection algorithm:
 * <ol>
 *   <li>Validates the block is a log type</li>
 *   <li>Checks for minimum vertical logs above</li>
 *   <li>Searches for natural leaves in configured radius</li>
 *   <li>Scans for player-placed blocks</li>
 * </ol>
 * 
 * @see TreeValidator
 * @see TreeScanner
 * @since 1.0.0
 */
public class NaturalTreeDetector implements TreeDetector {
    /**
     * Detects if the given block is part of a natural tree.
     * 
     * @param block The block that was broken by the player
     * @param player The player who broke the block
     * @return An {@link Optional} containing {@link TreeData} if the block is part
     *         of a natural tree, or {@link Optional#empty()} otherwise
     * @throws NullPointerException if block or player is null
     */
    @Override
    public Optional<TreeData> detectTree(@NotNull Block block, @NotNull Player player) {
        // Implementation
    }
}
```

### Wiki/README Sections
- Installation guide
- Configuration guide
- Permission setup
- API usage examples
- FAQ
- Troubleshooting
- Contributing guidelines

---

## API for Other Plugins

### Public Events

```java
/**
 * Called when ArcSylva detects a tree.
 * Cancellable.
 */
public class TreeDetectionEvent extends Event implements Cancellable {
    private final Player player;
    private final TreeData tree;
    private boolean cancelled = false;
    
    // Methods...
}

/**
 * Called when ArcSylva starts felling a tree.
 * Cancellable.
 */
public class TreeFellEvent extends Event implements Cancellable {
    // Similar structure
}

/**
 * Called when tree felling completes.
 * Not cancellable.
 */
public class TreeFelledEvent extends Event {
    // Methods...
}
```

### Public API Access

```java
public interface ArcSylvaAPI {
    /**
     * Gets the tree service for programmatic tree operations.
     */
    TreeService getTreeService();
    
    /**
     * Gets the block data repository for tracking player-placed blocks.
     */
    BlockDataRepository getBlockRepository();
    
    /**
     * Checks if a block would trigger tree felling.
     */
    boolean wouldFellTree(Block block, Player player);
    
    /**
     * Manually triggers tree felling.
     */
    CompletableFuture<BreakResult> fellTree(Block block, Player player);
}

// Access from another plugin:
ArcSylvaAPI api = (ArcSylvaAPI) Bukkit.getPluginManager().getPlugin("ArcSylva");
```

---

## Conclusion

This design represents **production-grade Minecraft plugin architecture**:

✅ **Clean Architecture** — Clear separation of concerns  
✅ **SOLID Principles** — Maintainable, extensible code  
✅ **Design Patterns** — Industry-standard solutions  
✅ **Performance** — Zero lag, optimized algorithms  
✅ **Safety** — Never breaks player builds  
✅ **Compatibility** — Works with all major plugins  
✅ **Testability** — Comprehensive test coverage  
✅ **Documentation** — Professional-grade docs  

---

