package etf.ri.rma.newsfeedapp.data.network

data class ImaggaApiResponse(
    val result: ImaggaResult,
    val status: ImaggaStatus
)

data class ImaggaStatus(
    val text: String,
    val type: String? = null
)
data class ImaggaResult(
    val tags: List<ImaggaTag>
)

data class ImaggaTagDetail(
    val en: String
)
data class ImaggaTag(
    val tag: ImaggaTagDetail,
    val confidence: Double
)

