/*
 * Copyright (C) 2010 Bartosch Warzecha, Matthias Weßendorf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wessendorf.cdi.transactional;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.wessendorf.logger.JulFactory;

/**
 * AOP via Java EE - (very) simple Interceptor
 * to use (declarative) Transactions via a custom
 * <code>Transactional</code> annotation, like you
 * know from Spring Framework
 */
@Transactional
@javax.interceptor.Interceptor
public class TransactionalInterceptor implements Serializable
{
  private static final long serialVersionUID = 1L;

  private @Inject EntityManager entityManager;
  private static Logger LOG = JulFactory.createLogger(TransactionalInterceptor.class);

  @AroundInvoke
  public Object invoke(InvocationContext context) throws Exception
  {
    EntityTransaction transaction = entityManager.getTransaction();

    try
    {
      if (!transaction.isActive())
      {
        transaction.begin();
      }

      return context.proceed();

    }
    catch (Exception e)
    {
      LOG.log(Level.SEVERE, "Exception in transactional method call", e);

      if (transaction != null)
      {
        transaction.rollback();
      }

      throw e;

    }
    finally
    {
      if (transaction != null && transaction.isActive())
      {
        transaction.commit();
      }
    }
  }
}