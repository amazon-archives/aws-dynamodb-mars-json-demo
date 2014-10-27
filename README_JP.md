## 機能
NASA ジェット推進研究所が発行した火星の画像を簡単に保存してインデックスを作成することができます。このプロジェクトには、DynamoDB または DynamoDB ローカルにデータを格納するための Java アプリケーションと、画像のやり取りや表示を行うフロントエンド Web アプリケーションが含まれています。低レベルの Java SDK を使用して DynamoDB に JSON データを格納する方法と、DynamoDB ドキュメントの SDK for JavaScript を使用して DynamoDB からデータのクエリを行う方法の例を示します。

## 使用開始
メタデータの一部を使用して DynamoDB ローカルのデモをローカルで実行するには、次のコマンドを実行してください
```
    > cd viewer
    > npm install
    > bower install
    > grunt serve
```

## 最小要件
- Java 1.7+
- NodeJS
  - npm
  - bower
  - grunt
  - coffee-script
- Ruby
  - compass
- Maven

## ソースからの作成
### イメージインジェスター
Maven を使用して Java アプリケーションをビルドすることができます。ディレクトリ 'ingester' に移動して、次のコマンドを実行してください
``` 
    > mvn clean install
```

### イメージビューアー
次のコマンドを使用して、フロントエンド Web アプリケーションをビルドおよび実行できます。
```
    > npm install
    > bower install
    > grunt build
```

## データソース/データインジェスションスキーマ

JSON イメージ データは [http://json.jpl.nasa.gov/data.json http://json.jpl.nasa.gov/data.json] からのものです。イメージインジェスターは、ディレクトリ 'photo_ingester' にあります。

## リリースノート