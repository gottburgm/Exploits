<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/">
        <result>
            <xsl:value-of select=
                "//node()[format-number(1, '', 'invalid-format')][1]"
            />
        </result>
    </xsl:template>
</xsl:stylesheet>

