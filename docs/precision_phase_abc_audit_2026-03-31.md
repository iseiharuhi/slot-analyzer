# 精度フェーズ監査レポート（A/B/C同時実施）

このレポートは、現行パッケージ内の `app/src/main/assets/machines/*.json` 全113件を読み、既存JSON・天井情報・メモ文を確認したうえでまとめた。今回の出力では、既存の機種JSONや manifest を上書きせず、まず「どこまで終わっているか」「どこが未完了か」を壊さず確定させることを優先した。

## 1. 全体結果

- 総機種数: 113
- 監査済み（既存完成寄り）: 47
- partial（比較値ありだが追加余地あり）: 29
- draft（比較値未登録）: 37

## 2. A: 北斗 完成版監査

- スマスロ北斗の拳（partial）: counters=8 / refs=4 / setting=1/2/3/4/5/6 / ceiling=2 / gaps=特記なし
- 判別要素としては「初当りBB」「弱スイカ」「強スイカ」「リーチ目役」の4本が実装済み。入力欄も8個あり、天井ルールも2件保持されている。
- ただしユーザー指示どおり、北斗は「完成扱い」ではなく、まだ詰め切っていない機種として扱うのが妥当。現状は partial 判定に固定した。
- 未完了理由: 終了画面・ボイス・高確/モード系など、実戦で強いが現在JSONに定量反映されていない要素が残っている。

## 3. B: 5機種まとめて精度強化の現状監査

- 甲鉄城のカバネリ（audited）: counters=6 / refs=3 / setting=1/2/3/4/5/6 / ceiling=3 / gaps=特記なし
- L革命機ヴァルヴレイヴ（audited）: counters=5 / refs=2 / setting=1/2/3/4/5/6 / ceiling=4 / gaps=特記なし
- Lからくりサーカス（partial）: counters=5 / refs=2 / setting=1/2/3/4/5/6 / ceiling=3 / gaps=特記なし
- Lゴブリンスレイヤー（partial）: counters=4 / refs=3 / setting=1/2/3/4/5/6 / ceiling=0 / gaps=天井未整理 / 未反映要素あり
- L戦国乙女4 戦乱に閃く炯眼の軍師（partial）: counters=4 / refs=3 / setting=1/2/3/4/5/6 / ceiling=0 / gaps=天井未整理

- カバネリは既存完成寄り。共通6枚ベルまで入っており、完成候補。
- ヴァルヴレイヴ / からくりは2指標構成のため、まだ「完成」まで持ち上げるには情報不足。
- ゴブスレ / 戦国乙女4 は partial として成立しているが、示唆・終了画面・補助指標の追加余地が大きい。

## 4. C: 完成済み機種の保護監査

既存で調整が進んでいる機種は、今回「完成寄りとして維持」「未完了機種とは分けて扱う」方針で棚卸しした。代表例は以下。

- A-SLOT+ 異世界かるてっと BT（refs=1 / ceiling=0）
- L ひぐらしのなく頃に 業（refs=5 / ceiling=0）
- L エヴァンゲリオン ～未来への創造～（refs=2 / ceiling=0）
- L リングにかけろ1 V（refs=3 / ceiling=0）
- LB スロット GALFY（refs=3 / ceiling=0）
- LBトリプルクラウンセブン（refs=3 / ceiling=0）
- Lアクダマドライブ（refs=3 / ceiling=0）
- Lパチスロ 炎炎ノ消防隊2（refs=2 / ceiling=0）
- Lラブ嬢3〜Wご指名はいかがですか？〜（refs=2 / ceiling=0）
- L大工の源さん超夢源（refs=2 / ceiling=0）
- L範馬刃牙（refs=2 / ceiling=0）
- L花の慶次～佐渡攻めの章〜（refs=2 / ceiling=0）
- L虚構推理（refs=2 / ceiling=0）
- L革命機ヴァルヴレイヴ（refs=2 / ceiling=4）
- Sちゅらちゅら（refs=3 / ceiling=0）
- アイムジャグラーEX（refs=3 / ceiling=0）
- アニマルスロット ドッチ（refs=3 / ceiling=0）
- クランキークレスト（refs=4 / ceiling=0）
- ゴーゴージャグラー3（refs=3 / ceiling=0）
- スマスロ BIRDIE WING -Golf Girls' Story-（refs=2 / ceiling=0）

## 5. セクション別の進捗

- 6.5号機〜AT系: audited=1 / partial=1 / draft=16
- DMM追加機: audited=33 / partial=0 / draft=0
- その他・バラエティ: audited=1 / partial=2 / draft=2
- スマスロ・AT/BT系: audited=1 / partial=6 / draft=19
- ノーマルタイプ: audited=8 / partial=9 / draft=0
- 既存個別機: audited=2 / partial=1 / draft=0
- 沖スロ・ハナハナ系: audited=1 / partial=10 / draft=0

## 6. 今回の結論

- 「終わっている機種もある」認識は正しい。少なくとも 21機種は既存JSON上で完成寄りの監査済みグループとして扱える。
- 一方で、北斗とからくりは現状のJSONだけではまだ partial 側に置くのが安全。
- 残りの大仕事は draft 37機種。ここは比較値ゼロなので、今後は外部公開情報を確認しながら1台ずつ上げる必要がある。
- 今回は既存の完成寄り機種を壊さず、全113機種の現状を確定するところまでを差分化した。