package cfy.vuln.app

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.realm.RealmModel
import io.realm.annotations.RealmClass
import io.realm.kotlin.types.RealmObject
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.NotNull


//Data class for Room DB
@Entity(tableName = "ProductTable")
data class Product(@PrimaryKey (autoGenerate = true) val id:Int?,
@ColumnInfo(name = "ProductName") val productName: String,
@ColumnInfo(name = "Price") val productPrice: Double,
@ColumnInfo(name = "ImageURL") val productImageURl: String,
@ColumnInfo(name = "ProductDescription") val productDescription: String,
)



@Entity(tableName = "OrderedItems")
data class OrderedItems(@PrimaryKey (autoGenerate = true) val id:Int?,
                        @ColumnInfo(name="Token") @NotNull val tokenId:String,
                        @ColumnInfo(name = "userID") val userID: Int?= 0,
                        @ColumnInfo(name = "OrderID") val orderID: String?= getRandomString(18),
                        @ColumnInfo(name = "Time") val time: String?=getCurrentTime(),
                        @ColumnInfo(name = "OrderStatus") val orderStatus: Int?=1,
                        @ColumnInfo(name = "TotalPrice") val totalPrice: Double?=1.0,
                        @ColumnInfo(name = "productList") val productList: String,
                        @ColumnInfo(name = "paymentID") val paymentID: String,
                        @ColumnInfo(name = "transactionID") val transactionID: String
)

