# ArcSylva

Smart natural tree felling for **Paper 1.21.x**. Break one log and ArcSylva safely fells the rest of a natural tree — without chewing through player builds.

## Features

- Detects natural trees (logs + leaves) with bounded flood scanning
- Tracks player-placed blocks via chunk PDC so builds stay safe
- Optional confirmation when a tree looks modified
- Sneak to disable, axe requirement, per-tick batch breaking (low TPS impact)
- Sapling replant, Fortune, and durability handling (configurable)
- Supports overworld logs and nether stems (including stripped variants)

## Requirements

| Component | Version |
|-----------|---------|
| Server | Paper (or compatible) 1.21.x |
| Java | 21+ |

## Install

1. Build or download the shaded jar from [Releases](https://github.com/uhhrin/ArcSylva/releases).
2. Drop `ArcSylva-*.jar` into your server `plugins/` folder.
3. Restart / reload. A default `config.yml` is written on first run.

## Build

```bash
./gradlew build
```

Shaded plugin jar: `build/libs/` (Shadow jar; `build` depends on `shadowJar`).

Dev server (optional):

```bash
./gradlew runServer
```

## Configuration

Key options in `plugins/ArcSylva/config.yml`:

| Key | Default | Meaning |
|-----|---------|---------|
| `require-axe` | `true` | Only fell when holding an axe |
| `sneak-to-disable` | `true` | Sneak to cut a single block normally |
| `confirm-modified-trees` | `true` | Ask again if player-placed logs are mixed in |
| `limits.max-blocks-per-tree` | `64` | Hard cap per fell |
| `limits.blocks-per-tick` | `12` | Batch size (lower = gentler on TPS) |
| `features.replant-sapling` | `true` | Replant after felling |
| `enabled-worlds` | `[]` | Empty = all worlds |

Full defaults ship in [`src/main/resources/config.yml`](src/main/resources/config.yml).

## Permissions

| Node | Description |
|------|-------------|
| `arcsylva.use` | Use tree felling |
| `arcsylva.confirm` | Receive modified-tree prompts |
| `arcsylva.bypass.limit` | Ignore max-blocks limit |
| `arcsylva.admin.reload` | Reload config |
| `arcsylva.admin.info` | Admin/debug info |

## How it works (short)

1. Player breaks a log (`MONITOR`, respects cancelled protection events).
2. Detector scans connected natural logs/leaves within configured limits.
3. Validators check permissions, tool, sneak, and player-placed tracking.
4. If needed, confirmation prompts; otherwise batch breaker removes the tree over multiple ticks.

Deeper architecture notes (design doc): [`docs/DESIGN.md`](docs/DESIGN.md).

## Releases

Push a tag matching `v*` (e.g. `v1.0.0`) to publish a GitHub Release with the plugin jar via Actions.

## License

No license file is bundled yet — all rights reserved unless you add one. Open an issue if you want a specific OSS license applied.
