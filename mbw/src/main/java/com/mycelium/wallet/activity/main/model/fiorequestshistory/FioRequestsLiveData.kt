package com.mycelium.wallet.activity.main.model.fiorequestshistory

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import android.os.AsyncTask
import com.mycelium.wallet.MbwManager
import com.mycelium.wapi.wallet.fio.FioGroup
import com.mycelium.wallet.event.*
import com.mycelium.wapi.wallet.fio.FioAccount
import com.squareup.otto.Subscribe
import fiofoundation.io.fiosdk.models.fionetworkprovider.FIORequestContent
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * This class is intended to manage transaction history for current selected account.
 */
class FioRequestsLiveData(val mbwManager: MbwManager) : LiveData<MutableList<FioGroup>>() {
    private var account = mbwManager.selectedAccount!!
    private var historyList = mutableListOf<FioGroup>()
    // Used to store reference for task from syncProgressUpdated().
    // Using weak reference as as soon as task completed it's irrelevant.
    private var syncProgressTaskWR: WeakReference<AsyncTask<Void, List<FioGroup>, List<FioGroup>>>? = null
    @Volatile
    private var executorService: ExecutorService

    init {
        value = historyList
        executorService = Executors.newCachedThreadPool()
        startHistoryUpdate()
    }

    fun appendList(list: Set<FioGroup>) {
        historyList.addAll(list)
        value = historyList
    }

    override fun onActive() {
        super.onActive()
        MbwManager.getEventBus().register(this)
        if (account !== mbwManager.selectedAccount) {
            account = mbwManager.selectedAccount
            updateValue(ArrayList())
        }
        startHistoryUpdate()
    }

    override fun onInactive() {
        MbwManager.getEventBus().unregister(this)
    }

    private fun startHistoryUpdate(): AsyncTask<Void, List<FioGroup>, List<FioGroup>> =
            UpdateTxHistoryTask().executeOnExecutor(executorService)


    /**
     * Leak might not occur, as only application context passed and whole class don't contains any Activity related contexts
     */
    @SuppressLint("StaticFieldLeak")
    private inner class UpdateTxHistoryTask : AsyncTask<Void, List<FioGroup>, List<FioGroup>>() {
        var account = mbwManager.selectedAccount!!
        override fun onPreExecute() {
            if (account.isArchived) {
                cancel(true)
            }
        }

        override fun doInBackground(vararg voids: Void): List<FioGroup> {
            if (account is FioAccount){
                return  (account as FioAccount).getRequestsGroups()
            }
            return emptyList()

        }

        override fun onPostExecute(transactions: List<FioGroup>) {
            if (account === mbwManager.selectedAccount) {
                updateValue(transactions)
            }
        }
    }

    private fun updateValue(newValue: List<FioGroup>) {
        historyList.clear()
        historyList.addAll(newValue)
        value = historyList
    }

    @Subscribe
    fun selectedAccountChanged(event: SelectedAccountChanged) {
        val oldExecutor = executorService
        executorService = Executors.newCachedThreadPool()
        oldExecutor.shutdownNow()
        if (event.account != account.id) {
            account = mbwManager.selectedAccount
            updateValue(ArrayList())
            startHistoryUpdate()
        }
    }

    @Subscribe
    fun syncStopped(event: SyncStopped) {
        startHistoryUpdate()
    }

    @Subscribe
    fun accountChanged(event: AccountChanged) {
        if (event.account == account.id) {
            startHistoryUpdate()
        }
    }

    @Subscribe
    fun balanceChanged(event: BalanceChanged) {
        if (event.account == account.id) {
            startHistoryUpdate()
        }
    }

    @Subscribe
    fun addressBookChanged(event: AddressBookChanged) {
        startHistoryUpdate()
    }

    @Subscribe
    fun transactionLabelChanged(event: TransactionLabelChanged) {
        startHistoryUpdate()
    }

    /**
     * This method might be called too frequently in process of sync, so it would not start
     * next task until previous finished.
     */
    @Subscribe
    fun syncProgressUpdated(event: SyncProgressUpdated) {
        val syncProgressTask = syncProgressTaskWR?.get()
        if (event.account == account.id && syncProgressTask?.status != AsyncTask.Status.RUNNING
                && syncProgressTask?.status != AsyncTask.Status.PENDING) {
            syncProgressTaskWR = WeakReference(startHistoryUpdate())
        }
    }
}