package bask.lingvino.utils

interface FirebaseDataCallback {
    fun onData(collections: MutableList<String>) { /* default implementation */ }
    fun onData(exists: Boolean) { /* default implementation */ }
}