package org.neo4j.scalastyle.rule;

import one.util.streamex.StreamEx;
import org.apache.commons.lang3.StringUtils;
import org.scalastyle.ScalastyleError;
import org.scalastyle.scalariform.IllegalImportsChecker;
import scala.collection.JavaConversions;
import scala.collection.JavaConverters;
import scala.collection.immutable.List;
import scala.collection.immutable.Map;
import scalariform.lexer.Token;
import scalariform.lexer.Tokens;
import scalariform.parser.CompilationUnit;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;

public class KernelImportChecker extends IllegalImportsChecker
{
    private static final String ERROR_KEY = "illegal.imports.kernel";

    private java.util.List<String> excludedPackages = Collections.emptyList();
    private java.util.List<String> excludedClassNames = Collections.emptyList();
    private String kernelImportPrefix = "org.neo4j.kernel";

    @Override
    public void setParameters( Map<String,String> parameters )
    {
        super.setParameters( parameters );
        excludedPackages = getParameterList( "excludedPackages" );
        excludedClassNames = getParameterList( "excludedClasses" );
        kernelImportPrefix = getString( "kernelImportPrefix", "org.neo4j.kernel" ).trim();
    }

    @Override
    public String errorKey()
    {
        return ERROR_KEY;
    }

    @Override
    public boolean matches( ImportClauseVisit t )
    {
        List<String> imports = imports( t );
        java.util.List<String> importsList = JavaConverters.seqAsJavaListConverter( imports ).asJava();
        return importsList.stream().anyMatch( importClass -> importClass.startsWith( kernelImportPrefix ) );
    }

    @Override
    public List<ScalastyleError> verify( CompilationUnit ast )
    {
        List<Token> tokens = ast.tokens();
        java.util.List<Token> tokenList = JavaConverters.seqAsJavaListConverter( tokens ).asJava();
        String packageName = getPackageName( tokenList );
        if ( !isExcludedPackage( packageName ) )
        {
            String className = getClassName( tokenList );
            if ( !isExcludedClass( packageName, className ) )
            {
                return super.verify( ast );
            }
        }
        return JavaConversions.asScalaBuffer( Collections.<ScalastyleError>emptyList() ).toList();
    }

    private java.util.List<String> getParameterList( String parameterName )
    {
        return Arrays.stream( getString( parameterName, StringUtils.EMPTY ).split( "," ) ).map( String::trim )
                .filter( StringUtils::isNotEmpty ).collect( Collectors.toList() );
    }

    private boolean isExcludedClass( String packageName, String className )
    {
        return excludedClassNames.contains( String.join( ".", packageName, className ) );
    }

    private boolean isExcludedPackage( String packageName )
    {
        return excludedPackages.stream().anyMatch( packageName::startsWith );
    }

    private String getPackageName( java.util.List<Token> tokenList )
    {
        return StreamEx.of( tokenList )
                    .filter( token -> (!token.tokenType().equals( Tokens.DOT() ) &&
                                       !token.tokenType().equals( Tokens.PACKAGE() ) ) )
                    .takeWhile( token -> token.tokenType().equals( Tokens.VARID() ) )
                    .map( Token::text )
                    .collect( Collectors.joining( "." ) );
    }

    private String getClassName( java.util.List<Token> tokenList )
    {
        Iterator<Token> tokenIterator = tokenList.iterator();
        while ( tokenIterator.hasNext() )
        {
            Token token = tokenIterator.next();
            if ( token.tokenType().equals( Tokens.CLASS() ) )
            {
                while (tokenIterator.hasNext())
                {
                    Token classNameTokenCandidate = tokenIterator.next();
                    if (classNameTokenCandidate.tokenType().equals( Tokens.VARID() ))
                    {
                        return classNameTokenCandidate.text();
                    }
                }
            }
        }
        return StringUtils.EMPTY;
    }
}
