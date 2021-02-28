package scripts

interface CommandEnum {
  val optionName: String
}

inline fun <reified T> Array<T>.userOptions(): String where T : CommandEnum, T : Enum<T> {
  return this.joinToString(", ") { it.optionName }
}