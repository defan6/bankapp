package com.bankapp.accountservice.adapters.out.jpa.mapper;


import com.bankapp.accountservice.adapters.out.jpa.AccountJpaEntity;
import com.bankapp.accountservice.domain.model.Account;
import com.bankapp.accountservice.domain.model.Money;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountJpaMapper {


    default Account toDomainAccount(AccountJpaEntity accountJpaEntity) {
        return Account.of(
                accountJpaEntity.getId(),
                accountJpaEntity.getUserId(),
                Money.of(accountJpaEntity.getBalance(), accountJpaEntity.getCurrency()),
                accountJpaEntity.getCurrency(),
                accountJpaEntity.getVersion(),
                accountJpaEntity.getCreatedAt(),
                accountJpaEntity.getUpdatedAt()
        );
    }


    default AccountJpaEntity toAccountJpaEntity(Account account){
        return new AccountJpaEntity(
                account.getId(),
                account.getUserId(),
                account.getBalance().getAmount(),
                account.getCurrency(),
                account.getVersion(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }
}
