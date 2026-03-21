package com.example.slotanalyzer.feature.session.domain.usecase

import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import javax.inject.Inject

class StartPlaySessionUseCase @Inject constructor(
    private val repository: PlaySessionRepository
) {
    /**
     * 方式B:
     * 機種選択時は既存セッションを維持するため、新規セッションは開始しない。
     *
     * 将来「新規実戦開始」ボタンを追加した時は、
     * startNewSession(machineId) を呼ぶ。
     */
    suspend operator fun invoke(machineId: String) {
        // 既存セッション維持のため何もしない
    }

    /**
     * 将来用:
     * 明示的に新しい実戦を開始したい時だけ呼ぶ。
     */
    suspend fun startNewSession(machineId: String) {
        repository.startSession(machineId)
    }
}