package com.example.studentmanagermvcandrxjava.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun Context.showToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

/** 统一的错误弹窗。 */
fun Activity.showError(message: String) {
    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
        .setTitleText("出错了")
        .setContentText(message)
        .show()
}

/** 把网络请求切到 IO 线程执行、主线程回调。 */
@SuppressLint("CheckResult")
fun <T> Single<T>.asyncRequest(): Single<T> =
    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

@SuppressLint("CheckResult")
fun Completable.asyncRequest(): Completable =
    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
