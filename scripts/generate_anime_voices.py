import asyncio, edge_tts, av, soundfile as sf, numpy as np, os

sound_dir = r"src/main/resources/assets/awakening_system/sounds/companion"
os.makedirs(sound_dir, exist_ok=True)
tmp_mp3 = "temp_voice.mp3"

characters = {
    "sukuna": {
        "voice": "ja-JP-KeitaNeural", "pitch": "-12Hz", "rate": "-5%",
        "summon": "おい... 伏魔御廚子へようこそ。存分に味わえ。",
        "skill": "領域展開... 伏魔御廚子！ 八つ裂きにしてやる。",
        "ambient": "頑張れ、頑張れ。王の前に跪け。",
        "hurt": "チッ... 戯れは終わりだ。"
    },
    "yuji_itadori": {
        "voice": "ja-JP-KeitaNeural", "pitch": "+15Hz", "rate": "+8%",
        "summon": "オッス！ 虎杖悠仁っす！ みんなを助けるために戦う！",
        "skill": "黒閃！ ぶっ飛ばす！",
        "ambient": "正しい死って何だろうな... でも、俺は負けない！",
        "hurt": "ぐっ... まだまだこれからだ！"
    },
    "megumi": {
        "voice": "ja-JP-KeitaNeural", "pitch": "+2Hz", "rate": "-2%",
        "summon": "伏黒恵だ。不平等に人を助ける。",
        "skill": "鵺！ 玉犬！ 喰らいつけ！",
        "ambient": "影の中に潜む... 警戒を怠るな。",
        "hurt": "くっ... まだ倒れるわけにはいかない！"
    },
    "artoria_pendragon": {
        "voice": "ja-JP-NanamiNeural", "pitch": "-2Hz", "rate": "+0%",
        "summon": "問おう、貴方が私のマスターか。サーヴァント・セイバー、召喚に応じ参上した。",
        "skill": "風王結界を解く... 約束された勝利の剣、エクスカリバー！",
        "ambient": "我が剣は貴方と共にあります。誇り高き勝利を掴みましょう。",
        "hurt": "くっ... まだ、諦めはしない！"
    },
    "gilgamesh": {
        "voice": "ja-JP-KeitaNeural", "pitch": "-4Hz", "rate": "-2%",
        "summon": "フハハハ！ 雑種め、この我を呼び出すとは身の程を知れ！",
        "skill": "宝物庫を開く！ ゲート・オブ・バビロン！ 灰燼に帰せ！",
        "ambient": "フン、世界の富はすべて我が物。貴様にも少し見せてやろう。",
        "hurt": "無礼者めが... 万死に値するぞ！"
    },
    "archer": {
        "voice": "ja-JP-KeitaNeural", "pitch": "-7Hz", "rate": "-4%",
        "summon": "サーヴァント・アーチャー。指示を頼む、マスター。",
        "skill": "体は剣で出来ている... アンリミテッド・ブレイドワークス！ 投影、開始！",
        "ambient": "理想を抱いて溺死しろ... とは言わんが、無理はするなよ。",
        "hurt": "甘いな... だが効いたぞ。"
    },
    "rengoku": {
        "voice": "ja-JP-KeitaNeural", "pitch": "+4Hz", "rate": "+10%",
        "summon": "炎柱・煉獄杏寿郎！ 心を燃やせ！ 共に戦おう！",
        "skill": "炎の呼吸、玖ノ型・煉獄！ 悪鬼滅殺！",
        "ambient": "うまい！ うまい！ うまい！ 腹が減っては戦はできんぞ！",
        "hurt": "胸を張って生きろ！ 傷など何するものぞ！"
    },
    "tanjiro": {
        "voice": "ja-JP-KeitaNeural", "pitch": "+12Hz", "rate": "+4%",
        "summon": "竈門炭治郎です！ 俺が必ず、みんなを守ってみせる！",
        "skill": "ヒノカミ神楽・円舞！ 燃え上がれ！",
        "ambient": "匂いで敵の気配がわかるんだ。油断しないで進もう！",
        "hurt": "痛い... だけど、挫けるな！ 頑張れ炭治郎！"
    },
    "nezuko": {
        "voice": "ja-JP-NanamiNeural", "pitch": "+12Hz", "rate": "+2%",
        "summon": "んー！ んーー！",
        "skill": "血鬼術・爆血！ んーーーっ！",
        "ambient": "ん？ んふふっ...",
        "hurt": "ふぎゃっ！ んーーーっ！"
    },
    "igris": {
        "voice": "ja-JP-KeitaNeural", "pitch": "-20Hz", "rate": "-8%",
        "summon": "主よ... 影の騎士イグリス、命により参上いたしました。",
        "skill": "深紅の剣閃！ 散れ、主の敵よ！",
        "ambient": "私は常に主の背後に控えております。ご安心を。",
        "hurt": "ぐっ... この程度で跪くものか！"
    },
    "beru": {
        "voice": "ja-JP-KeitaNeural", "pitch": "-2Hz", "rate": "+15%",
        "summon": "我が王よォォォ！ ベルがお傍にお仕えいたします！",
        "skill": "王の敵に死をォォ！ 捕食せよ！",
        "ambient": "王よ、陛下のためにこの世界をすべて焼き尽くしましょうか！",
        "hurt": "ギャアアッ！ 王に仇なす者に天罰を！"
    },
    "ellen_joe": {
        "voice": "ja-JP-NanamiNeural", "pitch": "-4Hz", "rate": "-6%",
        "summon": "ヴィクトリア家政のエレン... 早く終わらせてサボりたいんだけど。",
        "skill": "ハサミでチョキチョキ... 凍りつきな！",
        "ambient": "ロリポップ、もう一本ない？ 甘いもの食べないと動けない...",
        "hurt": "痛っ... めんどくさい連中ね。"
    }
}

def mp3_to_vorbis_ogg(mp3_in, ogg_out):
    container = av.open(mp3_in)
    frames = []
    for frame in container.decode(audio=0):
        frames.append(frame.to_ndarray())
    container.close()
    if not frames: return
    data = np.concatenate(frames, axis=1).T # (samples, channels)
    sf.write(ogg_out, data, 24000, format='OGG', subtype='VORBIS')

async def generate():
    for cid, conf in characters.items():
        print(f"\nSynthesizing Japanese lines for {cid} ({conf['voice']})...")
        for role in ["summon", "skill", "ambient", "hurt"]:
            line = conf[role]
            ogg_path = os.path.join(sound_dir, f"companion_{cid}_{role}.ogg")
            communicate = edge_tts.Communicate(line, conf["voice"], pitch=conf["pitch"], rate=conf["rate"])
            await communicate.save(tmp_mp3)
            mp3_to_vorbis_ogg(tmp_mp3, ogg_path)
            print(f"  -> Generated companion_{cid}_{role}.ogg ({os.path.getsize(ogg_path)} bytes)")

asyncio.run(generate())
if os.path.exists(tmp_mp3): os.remove(tmp_mp3)
print("\nAll 48 companion voice lines generated and converted to OGG Vorbis successfully!")
