/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 *
 * 第一问：最优路径长度计算
 */

package net.peratx.mathorcup

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

const val OFFSET = 750 //75cm偏移量
const val BOX_SIZE = 800 //货格长宽
const val PASS_WIDTH = OFFSET * 2
const val PASS_HEIGHT = 2000
const val SHELF_SIZE = 15
const val SHELF_HEIGHT = BOX_SIZE * SHELF_SIZE
const val SHELF_WIDTH = BOX_SIZE * 2
const val SHELF_OFFSET = 3000 // 初始位置
const val TABLE_SIZE = 1000

class Shelves(width: Int, height: Int) {
    val shelves = hashMapOf<Int, Shelf>()
    val tables = hashMapOf<Int, Table>()

    init {
        var cnt = -1
        for (i in 1..width) {
            for (j in height downTo 1) {
                cnt += 2
                shelves[cnt] = Shelf(i, height + 1 - j)
            }
        }

        tables[1] = Table(4000, 0)
        tables[2] = Table(8500, 0)
        tables[3] = Table(13000, 0)
        tables[4] = Table(17500, 0)
        tables[5] = Table(22000, 0)
        tables[6] = Table(26500, 0)
        tables[7] = Table(31000, 0)
        tables[8] = Table(35500, 0)
        tables[9] = Table(0, 3500)
        tables[10] = Table(0, 8000)
        tables[11] = Table(0, 12500)
        tables[12] = Table(0, 17000)
        tables[13] = Table(0, 21500)
    }

    /**
     * 计算两个格子之间距离
     * 如 calcDistance(S00101, S00102)
     */
    fun calcDistance(a: String, b: String): Int {
        if (a.toUpperCase() == b.toUpperCase()) {
            return 0
        }
        var distance = 0
        when (getPositionType(a, b)) {
            PositionType.BOX_TO_BOX -> {
                val boxA = a.getBox() //货格
                val boxB = b.getBox()
                val shelfA = a.getShelf() //货架
                val shelfB = b.getShelf()
                val shelfCoordA = shelves[shelfA.getRealShelf()]!! //货架相对坐标
                val shelfCoordB = shelves[shelfB.getRealShelf()]!!

                val absX = abs(shelfCoordA.x - shelfCoordB.x)
                //计算宽度
                if (shelfA.isSameSide(shelfB)) {
                    distance += absX * (PASS_WIDTH + SHELF_WIDTH) + PASS_WIDTH
                } else {
                    if (shelfCoordA == shelfCoordB) {//同一个货架，直接加上一个货架宽度和一个走廊宽度
                        distance += SHELF_WIDTH + PASS_WIDTH
                    } else {
                        distance += absX * SHELF_WIDTH // 货架宽度，先多加
                        if (shelfCoordA.x < shelfCoordB.x) {//shelfA处在左边
                            if (Math.floorMod(shelfA, 2) == 1 && Math.floorMod(shelfB, 2) == 0) {
                                //从左到右，要多加2个走廊宽度
                                distance += (absX + 2) * PASS_WIDTH + SHELF_WIDTH
                            } else {
                                //从右到左，减掉一个货架宽度
                                distance -= SHELF_WIDTH
                                distance += absX * PASS_WIDTH
                            }
                        } else {//shelfB处在左边，同样的算法
                            if (Math.floorMod(shelfB, 2) == 1 && Math.floorMod(shelfA, 2) == 0) {
                                //从左到右，要多加2个走廊宽度
                                distance += (absX + 2) * PASS_WIDTH + SHELF_WIDTH
                            } else {
                                //从右到左，减掉一个货架宽度
                                distance -= SHELF_WIDTH
                                distance += absX * PASS_WIDTH
                            }
                        }
                    }
                }
                val wid = distance
                //println("宽度：$distance")
                //计算高度
                val absY = abs(shelfCoordA.y - shelfCoordB.y)
                distance += if (absY == 0) {//同一个
                    if (shelfA == shelfB) {
                        abs(boxA - boxB) * BOX_SIZE
                    } else {
                        min(SHELF_SIZE - boxA + SHELF_SIZE - boxB + 1, boxA + boxB - 1) * BOX_SIZE + OFFSET * 2
                    }
                } else {
                    absY * PASS_HEIGHT + max(0, absY - 1) * SHELF_HEIGHT +
                            if (shelfCoordA.y > shelfCoordB.y) {//获取在上面的那个
                                (boxA + SHELF_SIZE - boxB) * BOX_SIZE
                            } else {
                                (boxB + SHELF_SIZE - boxA) * BOX_SIZE
                            }
                }
                //println("高度：${distance - wid}")
            }
            PositionType.BOX_TO_TABLE -> {
                val table = tables[(if (a.getType() == BoxType.TABLE) a else b).getTable().toInt()]!! //复核台
                val box = (if (a.getType() == BoxType.BOX) a else b) //货格
                val shelf = shelves[box.getShelf().getRealShelf()]!!
                val boxCoord = shelf.getCoordinate(box)
                if (table.isBottom()) {
                    distance += OFFSET //先加上一个出来的偏移
                    //出来之后的x
                    val vx = boxCoord.x + (if (box.getShelf().isLeft()) -OFFSET else OFFSET) //在左面-75，在右面+75
                    val tx = table.x + TABLE_SIZE / 2 //复核台中点坐标x
                    distance += abs(vx - tx)
                    //println("宽度 $distance")
                    val ty = table.y + TABLE_SIZE //复核台中点坐标y
                    distance += boxCoord.y - ty
                } else {
                    distance += boxCoord.x - (table.x + TABLE_SIZE)
                    if (!box.getShelf().isLeft()) {
                        distance += 2 * OFFSET //在右侧额外加两个750mm
                    }
                    //println("宽度 $distance")
                    distance += if (
                        (shelf.x == 1 && box.getShelf().isLeft()) ||
                        (shelf.getTopY() < table.y || shelf.getBottomY() > table.y)
                    ) {
                        abs(boxCoord.y - (table.y + TABLE_SIZE / 2))
                    } else {
                        val topUpLen = shelf.getTopY() - boxCoord.y + OFFSET //往上走的距离
                        val topDownLen = abs(shelf.getTopY() + OFFSET - (table.y + TABLE_SIZE / 2))

                        val bottomDownLen = boxCoord.y - shelf.getBottomY() + OFFSET
                        val bottomUpLen = abs(shelf.getBottomY() - OFFSET - (table.y + TABLE_SIZE / 2))
                        min(topUpLen + topDownLen, bottomDownLen + bottomUpLen)
                    }
                }
            }
            PositionType.TABLE_TO_TABLE -> {
                val tableA = tables[a.getTable().toInt()]!!
                val tableB = tables[b.getTable().toInt()]!!
                distance = abs(tableA.x - tableB.x) + abs(tableA.y - tableB.y)
            }
        }
        return distance
    }

    fun getPositionType(a: String, b: String): PositionType {
        //两者有一个是复核台的坐标即为到复核台类型
        if (a.getType() == BoxType.TABLE && b.getType() == BoxType.TABLE) {
            return PositionType.TABLE_TO_TABLE
        }
        if (a.getType() == BoxType.TABLE || b.getType() == BoxType.TABLE) {
            return PositionType.BOX_TO_TABLE
        }
        return PositionType.BOX_TO_BOX
    }
}

//获取输入类型
fun String.getType() =
    if (toUpperCase().startsWith("FH")) BoxType.TABLE else BoxType.BOX

fun String.getTable() = toUpperCase().replace("FH", "")

//获取货架
fun String.getShelf() = substring(1).toInt() / 100

//同一边吗？
fun Int.isSameSide(b: Int) = Math.floorMod(this, 2) == Math.floorMod(b, 2)

fun Int.isLeft() = Math.floorMod(this, 2) == 1

//获取货格
fun String.getBox() = substring(4, 6).toInt()

fun Int.getRealShelf() =
    if (Math.floorMod(this, 2) == 0) {
        this - 1 //是偶数就减一
    } else {
        this
    }


enum class BoxType {
    BOX, // 货格
    TABLE, // 复核台
}

enum class PositionType {
    BOX_TO_BOX, //到货格
    BOX_TO_TABLE, //到复核台的
    TABLE_TO_TABLE
}

data class Coordinate(val x: Int, val y: Int)

data class Shelf(val x: Int, val y: Int) {
    fun getCoordinate(a: String): Coordinate { //获取货格中点左边，总是在边上
        val box = a.getBox()
        var cx = x * SHELF_WIDTH + (x - 1) * PASS_WIDTH
        if (a.getShelf().isLeft()) {
            cx -= SHELF_WIDTH
        }
        val cy = (y - 1) * (SHELF_HEIGHT + PASS_HEIGHT) + (box - 1) * BOX_SIZE + BOX_SIZE / 2
        return Coordinate(cx + SHELF_OFFSET, cy + SHELF_OFFSET)
    }

    fun getTopY() =
        SHELF_OFFSET + (y - 1) * PASS_HEIGHT + (y + 1) * SHELF_HEIGHT

    fun getBottomY() =
        SHELF_OFFSET + (y - 1) * (PASS_HEIGHT + SHELF_HEIGHT)
}

data class Table(val x: Int, val y: Int) {
    fun isLeft() = x == 0

    fun isBottom() = y == 0
}
