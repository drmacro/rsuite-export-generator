<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:local="urn:ns:local-functions"
  xmlns:exslt-random="http://exslt.org/random"  
  xmlns:relpath="http://dita2indesign/functions/relpath"
  exclude-result-prefixes="xs local exslt-random relpath"
  version="2.0">
  <!-- ===========================================
       RSuite fake export generator
       
       Generates arbitrarily-large export data sets
       in order to do scale and performance testing
       of RSuite's import facility.
       
       Input is a "generation parameters" document
       that specifies the export parameters.
       
       Direct output is a report of what was generated.
       
       NOTE: Requires Saxon 9.6 or later in order to
       have random number support.
       ============================================= -->

  <xsl:import href="lib/relpath_util.xsl"/>
  
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:variable name="words" as="document-node()" select="document('data-files/words.xml')"/>
  <xsl:variable name="wordCount" as="xs:integer" select="count($words/*/*)"/>
  <xsl:variable name="seed" select="(seconds-from-dateTime(current-dateTime()) + .) * 1000" as="xs:double"/>
  
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="generation-parameters">
    <xsl:variable name="outdir" select="string(outdir)"/><!-- Directory to export to -->
    <xsl:variable name="browseWidth" select="if (browseWidth) then number(browseWidth) else 10" as="xs:double"/><!-- Number of top-level containers -->
    <xsl:variable name="browseDepth" select="if (browseDepth) then number(browseDepth) else 5" as="xs:double"/><!-- Maximum depth of any container path -->
    <xsl:variable name="maxContainerChildren" select="if (maxContainerChildren) then number(maxContainerChildren) else 100" as="xs:double"/><!-- Maximum number of children per container -->
    <xsl:variable name="maxXmlMOs" select="if (maxXmlMOs) then number(maxXmlMOs) else 10000" as="xs:double"/><!-- Maximum XML MOs to generate -->
    <xsl:variable name="maxBinaryMOs" select="if (maxBinaryMOs) then number(maxBinaryMOs) else 10000" as="xs:double"/><!-- Maxmim non-XML MOs to generate -->
    
    
    <xsl:variable name="resultUriRoot" as="xs:string"
      select="if (matches($outdir, '^(/|file:)')) 
                 then $outdir 
                 else resolve-uri($outdir, base-uri(.))"
    />
    
    <generation-report>
      <xsl:sequence select="."/>
      <outputdir><xsl:value-of select="$resultUriRoot"/></outputdir>
    </generation-report>
    
  </xsl:template>
  
  <xsl:template match="/*" priority="-1">
    <xsl:message terminate="yes">- [ERROR] The input document is not a 'generation-parameters' doc, got <xsl:value-of select="name(.)"/>.</xsl:message>
  </xsl:template>
  
  <!-- Get a random word from the word data file -->
  <xsl:function name="local:getRandomWord" as="xs:string">
    <!--      <xsl:message> + [DEBUG] seed="<xsl:value-of select="$seed"/>"</xsl:message>-->
    <xsl:variable name="wordIndex" as="xs:integer"
      select="xs:integer(exslt-random:random-sequence(1, $seed) * $wordCount)"
    />
    <xsl:variable name="result" as="xs:string"
      select="string($words/*/*[$wordIndex])"
    />
    <xsl:sequence select="$result"/>
  </xsl:function>
</xsl:stylesheet>