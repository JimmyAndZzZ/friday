package com.jimmy.friday.framework.annotation.transaction;

import com.jimmy.friday.framework.config.BootstrapConfig;
import com.jimmy.friday.framework.other.transaction.TransactionImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({BootstrapConfig.class, TransactionImportSelector.class})
public @interface EnableTransactional {
}
