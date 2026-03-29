package com.osnordev.abaco.di

import com.osnordev.abaco.data.repository.BudgetRepository
import com.osnordev.abaco.data.repository.BudgetRepositoryImpl
import com.osnordev.abaco.data.repository.ContactRepository
import com.osnordev.abaco.data.repository.ContactRepositoryImpl
import com.osnordev.abaco.data.repository.CurrencyRepositoryImpl
import com.osnordev.abaco.data.repository.JournalEntryRepositoryImpl
import com.osnordev.abaco.data.repository.ModuleRepositoryImpl
import com.osnordev.abaco.data.repository.OnboardingRepository
import com.osnordev.abaco.data.repository.OnboardingRepositoryImpl
import com.osnordev.abaco.data.repository.PaymentDueRepository
import com.osnordev.abaco.data.repository.PaymentDueRepositoryImpl
import com.osnordev.abaco.data.repository.PinRepository
import com.osnordev.abaco.data.repository.PinRepositoryImpl
import com.osnordev.abaco.data.repository.TaxConfigRepositoryImpl
import com.osnordev.abaco.data.repository.TransactionRepositoryImpl
import com.osnordev.abaco.domain.repository.CurrencyRepository
import com.osnordev.abaco.domain.repository.JournalEntryRepository
import com.osnordev.abaco.domain.repository.ModuleRepository
import com.osnordev.abaco.domain.repository.TaxConfigRepository
import com.osnordev.abaco.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindTaxConfigRepository(
        impl: TaxConfigRepositoryImpl
    ): TaxConfigRepository

    @Binds
    @Singleton
    abstract fun bindModuleRepository(
        impl: ModuleRepositoryImpl
    ): ModuleRepository

    @Binds
    @Singleton
    abstract fun bindPinRepository(
        impl: PinRepositoryImpl
    ): PinRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(
        impl: CurrencyRepositoryImpl
    ): CurrencyRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        impl: OnboardingRepositoryImpl
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindJournalEntryRepository(
        impl: JournalEntryRepositoryImpl
    ): JournalEntryRepository

    @Binds
    @Singleton
    abstract fun bindPaymentDueRepository(
        impl: PaymentDueRepositoryImpl
    ): PaymentDueRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(
        impl: BudgetRepositoryImpl
    ): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        impl: ContactRepositoryImpl
    ): ContactRepository
}
