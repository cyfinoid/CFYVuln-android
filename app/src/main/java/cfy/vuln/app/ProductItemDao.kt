package cfy.vuln.app

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery



//Room DB Queries
@Dao
interface ProductItemDao {

    @Query("SELECT * FROM PRODUCTTABLE")
    fun getAll():List<Product>

    @Query("SELECT * FROM ProductTable WHERE ProductName LIKE '%' || :name || '%' ")
    fun findByName(name:String):List<Product>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(product: Product)

    @Delete
    fun delete(product: Product)

    @Query("Delete FROM ProductTable")
    fun deleteAll()



    //Ordered Table
    @Query("SELECT * FROM OrderedItems")
    fun getAllOrdered():List<OrderedItems>


    @Query("UPDATE OrderedItems SET OrderStatus = :status WHERE OrderID = :orderId")
    fun updateOrderStatusOrderId(orderId: String, status: Int)


    @Query("SELECT * FROM OrderedItems WHERE OrderID LIKE :ID LIMIT 1")
    fun findByOrderId(ID:String):OrderedItems

    @Query("SELECT * FROM OrderedItems WHERE userID LIKE :id")
    fun findByID(id:Int):List<OrderedItems>

    @RawQuery
    fun getUsersByNameWithQuery(query: SupportSQLiteQuery): List<OrderedItems>
    @RawQuery
    fun getProductByNameWithQuery(query: SupportSQLiteQuery): List<Product>

    @RawQuery
    fun customQuery(query: SupportSQLiteQuery): List<Product>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertOrder(product: OrderedItems)

    @Delete
    fun deleteOrder(product: OrderedItems)

    @Query("Delete FROM OrderedItems")
    fun deleteAllOrder()

}