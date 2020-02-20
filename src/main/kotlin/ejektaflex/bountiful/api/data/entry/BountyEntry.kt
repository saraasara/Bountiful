package ejektaflex.bountiful.api.data.entry


import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import ejektaflex.bountiful.api.data.ITagString
import ejektaflex.bountiful.api.data.JsonBiSerializer
import ejektaflex.bountiful.api.ext.getUnsortedStringSet
import ejektaflex.bountiful.api.ext.hackyRandom
import ejektaflex.bountiful.api.ext.setUnsortedStringSet
import ejektaflex.bountiful.api.generic.IStageRequirement
import ejektaflex.bountiful.api.generic.IWeighted
import ejektaflex.bountiful.api.generic.ItemRange
import net.minecraft.nbt.*
import net.minecraftforge.common.util.INBTSerializable

abstract class BountyEntry : ITagString, JsonBiSerializer<BountyEntry>, INBTSerializable<CompoundNBT>,
        IStageRequirement, IWeighted {

    abstract var type: String

    open var content: String = ""

    @SerializedName("nbt_data")
    override var nbtString: String? = null

    open var stages: MutableList<String>? = null

    open var amountRange: ItemRange? = null

    @Transient open var amount: Int = amountRange?.min ?: 1



    open var unitWorth: Int = Integer.MIN_VALUE

    //@Expose(serialize = false)
    override var weight: Int = 100

    override fun requiredStages() = mutableListOf<String>()

    val randCount: Int
        get() = ((amountRange?.min ?: 1)..(amountRange?.max ?: Int.MAX_VALUE)).hackyRandom()

    val worthRange: ItemRange
        get() = ItemRange(
                unitWorth * (amountRange?.min ?: amount),
                unitWorth * (amountRange?.max ?: amount)
        )

    val averageWorth: Int
        get() = (worthRange.min + worthRange.max) / 2


    // Must override because overriding [nbtString]
    override val tag: CompoundNBT?
        get() = super.tag


    override fun serializeNBT(): CompoundNBT {
        return CompoundNBT().apply {
            putString("type", type)
            putString("content", content)
            putInt("unitWorth", unitWorth)
            tag?.let {
                this.put("nbt", it)
            }
            //putInt("amount", amount)
            stages?.let { setUnsortedStringSet("stages", it.toSet()) }
        }
    }

    override fun deserializeNBT(tag: CompoundNBT) {
        type = tag.getString("type")
        content = tag.getString("content")
        unitWorth = tag.getInt("unitWorth")
        if ("nbt" in tag) {
            nbtString = tag["nbt"]!!.toString()
        }
        //amount = tag.getInt("amount")
        stages = tag.getUnsortedStringSet("stages").toMutableList()
    }

    open val prettyContent: String
        get() = toString()

    open val contentObj: Any? = null

    fun isValid(): Boolean {
        val isNBTValid = (nbtString == null) || (tag != null)
        return contentObj != null && isNBTValid
    }

    val minValueOfPick: Int
        get() = unitWorth * (amountRange?.min ?: 1)

}