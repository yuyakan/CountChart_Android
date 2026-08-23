# ProGuard / R8 ルール
# 参考: https://developer.android.com/build/shrink-code

# デバッグ用にスタックトレースを読めるよう行番号を保持し、元ファイル名は隠す
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# このアプリは org.json（Android標準）を手動で使っており、
# リフレクションに依存するシリアライズは無いため、追加のkeepルールは不要。
