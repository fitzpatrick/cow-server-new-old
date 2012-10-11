/**
 * Approved for Public Release: 10-4800. Distribution Unlimited.
 * Copyright 2011 The MITRE Corporation,
 * Licensed under the Apache License,
 * Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wiredwidgets.cow.server.convert;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.convert.ConversionService;

/**
 * Exposes the conversion service to converters.
 * @author JKRANES
 */
public abstract class AbstractConverter implements BeanFactoryAware {

    private ConversionService converter;
    protected BeanFactory factory;

    /*
     * Note that the ConversionService bean CANNOT be injected directly,
     * because this creates circular reference problems -- individual converters
     * must be fully created before the service is created so they cannot depend on
     * the ConversionService.  So instead we use lazy-initialization and do not
     * wire the reference until the converter is actually invoked.
     */
    protected ConversionService getConverter() {
        if (converter == null) {
            converter = factory.getBean(ConversionService.class);
        }
        return converter;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.factory = beanFactory;
    }



}
