package com.kanbe1365.countergraph.ui

import androidx.lifecycle.ViewModel
import com.kanbe1365.countergraph.data.CountFile
import com.kanbe1365.countergraph.data.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** メニュー画面の状態。iOS の MenuViewModel に相当。 */
class MenuViewModel : ViewModel() {

    private val _files = MutableStateFlow(FileRepository.loadFiles())
    val files: StateFlow<List<CountFile>> = _files.asStateFlow()

    /** 詳細画面から戻ったときなどに最新のファイル一覧を読み直す。 */
    fun reload() {
        _files.value = FileRepository.loadFiles()
    }

    fun add() {
        _files.value = FileRepository.add(_files.value)
    }

    fun duplicate(file: CountFile) {
        _files.value = FileRepository.duplicate(_files.value, file)
    }

    fun move(from: Int, to: Int) {
        _files.value = FileRepository.move(_files.value, from, to)
    }

    fun remove(file: CountFile) {
        _files.value = FileRepository.remove(_files.value, file)
    }
}
