xquery version "3.1";

declare namespace art="http://mathling.com/art";
declare namespace this="http://www.w3.org/2005/xquery-local-functions";
declare namespace svg="http://www.w3.org/2000/svg";
declare namespace ixml="http://invisiblexml.org/NS";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace array="http://www.w3.org/2005/xpath-functions/array";
declare namespace math="http://www.w3.org/2005/xpath-functions/math";
declare namespace saxon="http://saxon.sf.net/";
declare namespace cs="http://nineml.com/ns/coffeesacks";
declare namespace output="http://www.w3.org/2010/xslt-xquery-serialization";

declare option output:indent "yes";

declare function this:apply(
  $ixml as function(xs:string) as item(),
  $input as xs:string
) as element()
{
  let $result := (  
    try {
      $ixml($input)/*
    } catch * {
      error(xs:QName("art:NOPARSE"), ($input||" "||string($err:code)||" "||$err:description) )
    }
  )
  return (
    if ($result/@ixml:state="failed")
    then error(xs:QName("art:NOPARSE"), ($input||" "||serialize($result)))
    else $result
  )
};

declare function this:grammar(
  $grammar as xs:string,
  $options as map(xs:string,item())
) as function(xs:string) as item()
{
  try {
    cs:make-parser($grammar, $options)
  } catch * {
    trace((),
      serialize(
        cs:hygiene-report($grammar, $options)/*
      )
    ),
    error(xs:QName("art:NOPARSE"), ($grammar||" "||string($err:code)||" "||$err:description))
  }
};

declare function this:grammar(
  $grammar as xs:string
) as function(xs:string) as item()
{
  this:grammar($grammar, map { "ignoreTrailingWhitespace": true() })
};

declare function this:merge-into($maps as map(*)*) as map(*)
{
  map:merge($maps, map { "duplicates" : "use-last" })
};

declare function this:stochastic(
  $rules as map(xs:string,item())
) as function(element(), map(*)) as map(*)
{
  let $converted :=
    this:merge-into((
      for $key in map:keys($rules) return (
        if ($rules($key) instance of map(*)) then (
          map {
            $key: (: dist:weighted-distribution-of:)($rules($key))
          }
        ) else ()
      )
    ))
  return (
    function($context as element(), $options as map(*)) as map(*) {
      map {
        "whatever": serialize($converted($context/@name), map{'method':'json'}),
        "selection": (
          trace((), serialize($context)),
          (: XYZZY Uncomment next line and watch all hell break loose: :)
          trace((), serialize($converted($context/@name), map{'method':'json'})),
          (: rand:select-random($context/children/@id) :)
          head(random-number-generator(generate-id(<hack/>))?permute($context/children/@id))
        )
      }
    }
  )
};

declare function this:stochastic-lsystem(
  $generations as xs:integer,
  $start as xs:string,
  $rules as map(xs:string, item())
) as xs:string
{
  let $symbols := distinct-values(
    string-to-codepoints(
      string-join((
        map:keys($rules),
        for $key in map:keys($rules)
        let $rule := $rules($key)
        return (
          if ($rule instance of map(*)) then map:keys($rule) else $rule
        )
      ))
    )
  )!codepoints-to-string(.)
  let $non-terminals := $symbols[matches(., "[\p{L}]")]
  let $terminals := $symbols[not(matches(., "[\p{L}]"))]
  let $grammar := string-join((
    "root = ("|| (
      string-join(($non-terminals, $terminals!("'"||.||"'")), "|")
    ) || ")+ .",
    for $key in $non-terminals return (
      if (exists($rules($key))) then (
        if ($rules($key) instance of map(*)) then (
          $key||" = "||string-join(
            for $subkey in map:keys($rules($key)) return
              "-'"||$key||"', +'"||$subkey||"' "
            ,
            " | "
          )||" ."
        ) else $key||" = -'"||$key||"', +'"||$rules($key)||"' ."
      )
      else $key||" = '"||$key||"' ."
    )
    ), "
")
  let $options := (
    map {
      "ignoreTrailingWhitespace": true(),
      "choose-alternative": this:stochastic($rules)
    }
  )
  let $system := ( trace((), serialize($grammar)), this:grammar($grammar, $options) )
  let $final-string := (
    fold-left(1 to $generations, $start,
      function($string as xs:string, $generation as xs:integer) as xs:string {
        string($system=>this:apply($string))
      }
    )
  )
  return string($system($final-string)/*)
};

let $generations := 4
let $start := "FX+FX+"
return (
  <one>{
  this:stochastic-lsystem(
    $generations,
    "FX+FX+", map { "X": map {"X+XF": 80, "X-XF": 20}, "Y": "FX-Y" }
  )
  }</one>,
  <two>{
  this:stochastic-lsystem(
    $generations,
    "FX+FX+", map { "X": map {"X+XF": 80, "X-XF": 20}, "Y": "FX-Y" }
  )
  }</two>
)
