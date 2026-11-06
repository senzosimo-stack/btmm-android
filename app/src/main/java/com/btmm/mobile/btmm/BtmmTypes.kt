package com.btmm.mobile.btmm

enum class Timeframe { M15, H1, H4 }

enum class Bias { NONE, LONG, SHORT }

enum class BtmmState {
    NONE,
    ANCHOR,
    TRAP,
    STATIC,
    CROSS,
    TDI,
    SAFETY,
    AR
}

enum class TdiReload { NONE, OVERSOLD, OVERBOUGHT, SQUEEZE }

enum class DwatTier { T1, T2, T3, T4, T5, T6, T7 }

data class Candle(
    val time: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long
)

data class PairInfo(
    val symbol: String,
    val name: String,
    val digits: Int,
    val pip: Double,
    val base: Double,
    val seed: Long
)

data class RuleResult(
    val id: Int,
    val name: String,
    val short: String,
    val pass: Boolean
)

data class VolumeArea(
    val vah: Double,
    val poc: Double,
    val val: Double,
    val npoc: Double?,
    val npocAgeHours: Int
)

data class DailyRange(
    val high: Double,
    val low: Double,
    val range: Double,
    val mid: Double
)

data class Sessions(
    val asia: Boolean,
    val london: Boolean,
    val ny: Boolean,
    val asiaHigh: Double,
    val asiaLow: Double
)

data class GhostTrade(
    val dir: Bias,
    val entry: Double,
    val sl: Double,
    val tp: Double,
    val lots: Double,
    val riskAmt: Double,
    val rr: Double
)

data class Position(
    val dir: Bias,
    val entry: Double,
    val sl: Double,
    val tp: Double,
    val lots: Double,
    val openedAt: Long,
    val openedBar: Int,
    val comment: String,
    val peakR: Double,
    val symbol: String,
    val ticket: Int
)

data class ClosedTrade(
    val dir: Bias,
    val entry: Double,
    val exit: Double,
    val lots: Double,
    val pnl: Double,
    val state: BtmmState,
    val openedAt: Long,
    val closedAt: Long
)

data class ScanRow(
    val symbol: String,
    val bias: Bias,
    val score: Int,
    val comment: String,
    val price: Double,
    val change: Double
)

data class NewsItem(
    val title: String,
    val impact: NewsImpact,
    val time: Long,
    val locked: Boolean
)

enum class NewsImpact { RED, ORANGE }

data class EngineSnap(
    val price: Double,
    val bid: Double,
    val ask: Double,
    val spreadPts: Int,
    val ema8: Double,
    val ema50: Double,
    val ema200: Double,
    val ema1200: Double,
    val ema8Prev: Double,
    val atr: Double,
    val atrPips: Double,
    val atrPct: Double,
    val rsi: Double,
    val tdiSig: Double,
    val tdiUp: Double,
    val tdiLo: Double,
    val mfi: Double,
    val bias: Bias,
    val biasScore: Int,
    val cond200: Boolean,
    val cond50: Boolean,
    val cond8: Boolean,
    val state: BtmmState,
    val rules: List<RuleResult>,
    val gateOpen: Boolean,
    val rulesPassed: Int,
    val tier: DwatTier,
    val dwatScore: Int,
    val riskPct: Double,
    val tdiReload: TdiReload,
    val mfiConfirm: Boolean,
    val today: DailyRange,
    val yest: DailyRange,
    val profile: VolumeArea,
    val sessions: Sessions,
    val ghost: GhostTrade?,
    val mtf: MtfBias,
    val news: NewsItem?,
    val hod: Double,
    val lod: Double
)

data class MtfBias(
    val m15: Bias,
    val h1: Bias,
    val h4: Bias,
    val d1: Bias
)

data class World(
    val symbol: String,
    val tf: Timeframe,
    val candles: List<Candle>,
    val markets: Map<String, List<Candle>>,
    val positions: List<Position>,
    val closed: List<ClosedTrade>,
    val dailyPL: Double,
    val equity: List<Double>,
    val seedTrades: List<ClosedTrade>,
    val barStartedAt: Long,
    val simTime: Long,
    val paused: Boolean,
    val compact: Boolean,
    val helpOpen: Boolean,
    val density: Density,
    val lastScanAt: Long,
    val scanner: List<ScanRow>,
    val stateLog: List<StateLogEntry>,
    val events: List<BtmmEvent>,
    val eventSeq: Int,
    val visited: List<BtmmState>,
    val account: Double,
    val mode: Mode,
    val liveStatus: LiveStatus,
    val nextTicket: Int
)

enum class Density { FULL, COMPACT, TINY }

enum class Mode { LIVE, SIM }

enum class LiveStatus { DISCONNECTED, CONNECTING, CONNECTED }

data class StateLogEntry(val t: Long, val from: BtmmState, val to: BtmmState)

data class BtmmEvent(
    val id: Int,
    val kind: EventKind,
    val title: String,
    val body: String
)

enum class EventKind { STATE, ENTRY, EXIT, SCAN, NEWS, INFO }

val STATES: List<BtmmState> = listOf(
    BtmmState.ANCHOR,
    BtmmState.TRAP,
    BtmmState.STATIC,
    BtmmState.CROSS,
    BtmmState.TDI,
    BtmmState.SAFETY,
    BtmmState.AR
)

val STATE_LABEL: Map<BtmmState, String> = mapOf(
    BtmmState.NONE to "NONE",
    BtmmState.ANCHOR to "ANCHOR",
    BtmmState.TRAP to "TRAP",
    BtmmState.STATIC to "STATIC",
    BtmmState.CROSS to "CROSS",
    BtmmState.TDI to "TDI",
    BtmmState.SAFETY to "SAFETY",
    BtmmState.AR to "AR"
)

val TIER_NAME: Map<DwatTier, String> = mapOf(
    DwatTier.T1 to "Micro",
    DwatTier.T2 to "Small",
    DwatTier.T3 to "Base",
    DwatTier.T4 to "Elevated",
    DwatTier.T5 to "High",
    DwatTier.T6 to "Extreme",
    DwatTier.T7 to "Max"
)

val TIER_RISK: Map<DwatTier, Double> = mapOf(
    DwatTier.T1 to 0.25,
    DwatTier.T2 to 0.5,
    DwatTier.T3 to 1.0,
    DwatTier.T4 to 1.5,
    DwatTier.T5 to 2.0,
    DwatTier.T6 to 3.0,
    DwatTier.T7 to 5.0
)

const val BAR_REAL_MS = 7000L

val BAR_SIM_MS: Map<Timeframe, Long> = mapOf(
    Timeframe.M15 to 15 * 60 * 1000,
    Timeframe.H1 to 60 * 60 * 1000,
    Timeframe.H4 to 4 * 60 * 60 * 1000
)

const val ACCOUNT_START = 10000.0
const val MAX_DAILY_RISK = 3.0
const val MAX_SPREAD = 30
const val MIN_RR = 1.5
const val MAGIC_BARS = 1280
const val VISIBLE_BARS = 86
