package com.example.slotanalyzer.feature.session.domain.usecase

import com.example.slotanalyzer.feature.session.domain.model.UpdateCounterCommand
import com.example.slotanalyzer.domain.repository.PlaySessionRepository
import javax.inject.Inject

class UpdateCounterUseCase @Inject constructor(
    private val repository: PlaySessionRepository
) {
    suspend operator fun invoke(command: UpdateCounterCommand) = repository.updateCounter(command)
}
