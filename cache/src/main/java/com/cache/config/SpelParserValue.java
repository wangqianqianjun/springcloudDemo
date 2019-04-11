package com.cache.config;

import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;

/**
 * Created by wangqianjun on 2019/3/26.
 */
public class SpelParserValue {

    /**
     * 进行springEL解析
     *
     * @param parserStr 需要解析的字符串
     * @param method    目标方法
     * @param args      目标方法上的参数
     * @return
     */
    public static Object expressionParser(String parserStr, Method method, Object[] args) {
        //2.拿到key,解析springEL表达式
        ExpressionParser parser = new SpelExpressionParser();
        Expression expression = parser.parseExpression(parserStr);

        //参数
        EvaluationContext context = new StandardEvaluationContext();

        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        String[] parameterNames = discoverer.getParameterNames(method);
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        //解析
        return expression.getValue(context);
    }
}
