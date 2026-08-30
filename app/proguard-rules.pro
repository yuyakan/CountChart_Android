# ProGuard / R8 ルール
# 参考: https://developer.android.com/build/shrink-code

# デバッグ用にスタックトレースを読めるよう行番号を保持し、元ファイル名は隠す
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# このアプリは org.json（Android標準）を手動で使っており、
# リフレクションに依存するシリアライズは無いため、追加のkeepルールは不要。

# --- WorkManager / Room ---
# play-services-ads が推移的に androidx.work:work-runtime:2.7.0 →
# androidx.room:room-runtime:2.2.5 を引き込む。Room 2.2.5 は R8 fullMode 用の
# consumer ルールを同梱しておらず、R8 が Room 生成クラス（*_Impl）や
# そのコンストラクタをリネーム/削除するため、起動時に
# 「Failed to create an instance of androidx.work.impl.WorkDatabase」で
# クラッシュする。以下でリフレクションから参照されるクラスを保持する。
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { <init>(); }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class androidx.work.** { *; }
-dontwarn androidx.room.paging.**
