package usdpl


enum class Platform(private val platform: String)
{
	Any("any"),
	Decky("decky");

	companion object {
		fun current(): Platform
		{
			return Decky
		}
	}

	override fun toString(): String
	{
		return this.platform
	}

}