interface Displayeable {
	fun display(): String
}

data class TodoItem(
	val description: String,
	var completed: Boolean
): Displayeable {
	override fun display(): String {
		val sign = if (completed) "✅" else "❌"
		return sign + "\t"+ description
	}
}

fun main() {
	var items: MutableList<TodoItem> = mutableListOf()

	while (true) {
		println("==============================")
		println("Welcome!")
		println("------------------------------")
		if (items.count() == 0) {
			println("No items yet!")
		}
		else {
			items.asSequence()
				.mapIndexed{ i,e ->
					var sb = StringBuilder()
					sb.append(i + 1).append(") ").append(e.display())
					sb.toString()
				} 
				.forEach(::println)
		}
		println("------------------------------")
		println("A) Add a todo.")
		println("B) Remove task")
		println("C) Quit")

		println("Please select an option:")
		val option = readLine()

		if (option == "C") {
			break
		} else if (option == "A") {
			println("Creating new task...")
			println("Description:")
			val description = readLine()

			items.add(TodoItem(description!!, false))
		} else if (option == "B") {
			println("Removing task...")
			println("Enter number of task to remove:")

			val i = getIndex(readLine(), items)	
			if (i == null) continue else items.removeAt(i)
		} else {
			val i = getIndex(option, items)	
			if (i == null) continue else items.get(i).completed = !items.get(i).completed
		}
	}
}

fun getIndex(line: String?, items: List<Any>): Int? {
	val i = line?.toIntOrNull()?.minus(1)

	return if (i == null || i<0 || i>=items.count()) {
		println("Please write a valid number!")
		println("Press enter to continue...")
		readLine()
		null
	}
	else {
		i
	}
}
