data class ItemData(
    val originalPos: Int,
    val originalValue: Any,
    val type: String?,
    val info: String?) {
    override fun toString(): String = "[" + originalPos + ": " + originalValue + " - " + type + "/" + info + "]"
}

fun main() {
    val input: List<Any?> = listOf("Hola", 2, null, true, 1233, 546)
    val output: List<ItemData>? = processList(input)
    
    output!!.forEach(::print)
}

fun processList(list: List<Any?>?): List<ItemData>? {
    if (list == null) {
        return null
    }
    
    return list.asSequence()
        .mapIndexed{i, e -> mapToItemData(i, e)}
        .filterNotNull()
        .toList()
}

fun mapToItemData(index: Int, item: Any?): ItemData? {
    if (item == null) {
        return null
    }
    
    when (item) {
        is Boolean -> {
            return ItemData(index, item, "boolean", if (item) "Verdadero" else "Falso")
        }
        is Int -> {
            var info: String? = null
            if (item%10 == 0) {
                info = "M10"
            } else if (item%5 == 0){
                info = "M5"
            } else if (item%2 == 0) {
                info = "M2"
            }
            
            return ItemData(index, item, "entero", info)
        }
        is String -> {
            return ItemData(index, item, "cadena", "L" + item.length)
        }
        else -> {
            return ItemData(index, item, null, null)
        }
    }
}
