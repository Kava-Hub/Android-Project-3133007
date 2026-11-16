package com.example.fitness_tracker.ui.onboarding

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fitness_tracker.AthlimaApplication
import com.example.fitness_tracker.data.FitnessRepository
import com.example.fitness_tracker.domain.MoveGoalRules
import com.example.fitness_tracker.domain.ProfileDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class OnboardingStep { Welcome, Profile, MoveGoal, Notifications }
internal data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.Welcome,
    val draft: ProfileDraft = ProfileDraft(),
    val moveGoal: Int = 150,
    val errors: Map<String, String> = emptyMap(),
)

internal class OnboardingViewModel(
    private val repository: FitnessRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {
    private val profile = repository.state.value.profile
    private val mutableState = MutableStateFlow(OnboardingState(
        step = OnboardingStep.valueOf(savedState["step"] ?: OnboardingStep.Welcome.name),
        draft = ProfileDraft(
            savedState["birthDate"] ?: profile?.dateOfBirth?.toString().orEmpty(),
            savedState["sex"] ?: profile?.sex?.name.orEmpty(),
            savedState["height"] ?: profile?.heightCm?.toString().orEmpty(),
            savedState["weight"] ?: profile?.weightKg?.toString().orEmpty(),
        ),
        moveGoal = savedState["moveGoal"] ?: if (repository.state.value.goalsConfirmed) repository.state.value.goals.move else 150,
    ))
    val state = mutableState.asStateFlow()

    private fun update(next: OnboardingState) {
        savedState["step"] = next.step.name
        savedState["birthDate"] = next.draft.birthDate
        savedState["sex"] = next.draft.sex
        savedState["height"] = next.draft.height
        savedState["weight"] = next.draft.weight
        savedState["moveGoal"] = next.moveGoal
        mutableState.value = next
    }
    fun edit(draft: ProfileDraft) = update(state.value.copy(draft = draft, errors = emptyMap()))
    fun selectGoal(value: Int) = update(state.value.copy(moveGoal = value.coerceIn(MoveGoalRules.MIN, MoveGoalRules.MAX)))
    fun adjustGoal(direction: Int) = selectGoal(MoveGoalRules.adjust(state.value.moveGoal, direction))
    fun back() {
        val previous = (state.value.step.ordinal - 1).coerceAtLeast(0)
        update(state.value.copy(step = OnboardingStep.entries[previous], errors = emptyMap()))
    }
    fun next() {
        val current = state.value
        when (current.step) {
            OnboardingStep.Welcome -> update(current.copy(step = OnboardingStep.Profile))
            OnboardingStep.Profile -> {
                val errors = current.draft.errors()
                if (errors.isNotEmpty()) update(current.copy(errors = errors))
                else {
                    repository.saveProfile(current.draft.toProfile())
                    update(current.copy(step = OnboardingStep.MoveGoal))
                }
            }
            OnboardingStep.MoveGoal -> {
                repository.saveGoals(repository.state.value.goals.copy(move = current.moveGoal))
                update(current.copy(step = OnboardingStep.Notifications))
            }
            OnboardingStep.Notifications -> Unit
        }
    }
    fun finish() = repository.completeOnboarding()

    companion object {
        val Factory = viewModelFactory {
            initializer { OnboardingViewModel((this[APPLICATION_KEY] as AthlimaApplication).repository, createSavedStateHandle()) }
        }
    }
}
