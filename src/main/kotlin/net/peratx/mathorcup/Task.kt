/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 */

package net.peratx.mathorcup

data class Task(
    val order: String,
    val box: String,
    val cnt: Int
)

data class TaskGroup(
    val group: String,
    val tasks: ArrayList<Task> = ArrayList()
)

fun getTaskGroup(grp: String, d: String): TaskGroup {
    val group = TaskGroup(grp)
    d.split("\n").forEach {
        val data = it.split("\t")
        group.tasks += Task(data[0], data[1], data[2].toInt())
    }
    return group
}
