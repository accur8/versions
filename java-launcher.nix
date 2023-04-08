{
  fetchurl,
  linkFarm,
  jdk8,
  jdk11,
  jdk17,
  stdenv,
  unzip,
}:

  let

    launcherConfig = 
      {
      
        name = "a8-versions";
        mainClass = "a8.versions.apps.Main";
        jvmArgs = [];
        args =  [];
        repo = "repo";
        organization = "io.accur8";
        artifact = "a8-versions_2.13";
        version = "latest";
        branch = "master";
        webappExplode = null;
        javaVersion = null;
      
        dependencies = [
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/utils/2.19.6/utils-2.19.6.jar";  sha256 = "0pdnnv25qp92db8adfm9krmq9ag553qf2yqf4j8z4h5vs3ycsml3";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-resolver-dns-native-macos/4.1.82.Final/netty-resolver-dns-native-macos-4.1.82.Final-osx-x86_64.jar";  sha256 = "0ryf7rfagh906jm5mnfyvhipi49hnly1r1c8wbzw6rg32dy3xv8w";  }
          { url = "https://locus.accur8.net/repos/all/org/latencyutils/LatencyUtils/2.0.3/LatencyUtils-2.0.3.jar";  sha256 = "1v35f6jsazrlc7rxhg1pbm54bnf5gggzky30acff1x5j0vx9yam3";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-transport-native-unix-common/4.1.86.Final/netty-transport-native-unix-common-4.1.86.Final.jar";  sha256 = "05gi681csblci58pj2r9gg23ykgffwv7jzwrgvar2msn0qxd09pc";  }
          { url = "https://locus.accur8.net/repos/all/io/accur8/a8-sync-api_2.13/1.0.0-20230108_1051_master/a8-sync-api_2.13-1.0.0-20230108_1051_master.jar";  sha256 = "1ifx3hwphs2jawq8a5viki6ybqh2lr1nlpa912bnprs9wramgq6w";  }
          { url = "https://locus.accur8.net/repos/all/org/slf4j/slf4j-api/2.0.5/slf4j-api-2.0.5.jar";  sha256 = "1jglg8w946rrcqf45mlm8192npp1b46rp9x4zm4wq6i9152rg8pl";  }
          { url = "https://locus.accur8.net/repos/all/com/softwaremill/sttp/model/core_2.13/1.5.3/core_2.13-1.5.3.jar";  sha256 = "1xpwgx2l3gx8wvj00mba5fszjjrb6nchrppk6ksxrzw856fq32ah";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-internal-macros_2.13/2.0.5/zio-internal-macros_2.13-2.0.5.jar";  sha256 = "1iv4bdhm85q47xl50al2gh3cx2k56krrgyydjdvf7h8mgl9iq2d9";  }
          { url = "https://locus.accur8.net/repos/all/com/softwaremill/sttp/shared/core_2.13/1.3.10/core_2.13-1.3.10.jar";  sha256 = "0800nff8zz1js5fl73hc7nbqwkfyy3jbb4s79x6szn18xq7l1afs";  }
          { url = "https://locus.accur8.net/repos/all/com/fasterxml/jackson/core/jackson-annotations/2.13.4/jackson-annotations-2.13.4.jar";  sha256 = "0i1vcpy0a72hcc904x014jd4kw8xn1yywl1q275928wl6jk2fnxc";  }
          { url = "https://locus.accur8.net/repos/all/com/beachape/enumeratum_2.13/1.7.0/enumeratum_2.13-1.7.0.jar";  sha256 = "17mvgm9vdydix6ml84w8nm4vbkzkv1wi0aw5p0y03b7pig5n5lkm";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-resolver/4.1.86.Final/netty-resolver-4.1.86.Final.jar";  sha256 = "1fpxrdmswr3jxfcsk03gc6wq2acy4rzkm4nq87f4693zklqa2a3n";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-codec-socks/4.1.82.Final/netty-codec-socks-4.1.82.Final.jar";  sha256 = "1cy7y0c9n1pswpd7mxxswz1ncxzy77pfsz1hykzd1wyvqir4dzfy";  }
          { url = "https://locus.accur8.net/repos/all/com/softwaremill/sttp/core_2.13/1.7.2/core_2.13-1.7.2.jar";  sha256 = "12k1bwdvbminr2ndlz71pzrivb6qwwz4yhxwxmiby71hnbz5vb1d";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/route53/2.19.6/route53-2.19.6.jar";  sha256 = "03l6k7b979yp6xw6cmckngkwg7m9glsb49xdrrswz40i6y86jxkk";  }
          { url = "https://locus.accur8.net/repos/all/org/scala-lang/scala-reflect/2.13.8/scala-reflect-2.13.8.jar";  sha256 = "1r22wk8pjn77r4301nmnqj8fkzjm1xzb9g03ndw4ahkzx29cryzx";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-tcnative-boringssl-static/2.0.54.Final/netty-tcnative-boringssl-static-2.0.54.Final-osx-x86_64.jar";  sha256 = "1zbm8a772l3r9w3l0vjb61b1yry26appncg5kjr7mr5ddp5cvdj9";  }
          { url = "https://locus.accur8.net/repos/all/com/fasterxml/jackson/core/jackson-databind/2.13.4/jackson-databind-2.13.4.jar";  sha256 = "1dkyh23qsg99bhf229r0jk4k4sjhqazfxnqi8wg7wb4y1m1gzyn9";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-tcnative-boringssl-static/2.0.54.Final/netty-tcnative-boringssl-static-2.0.54.Final-windows-x86_64.jar";  sha256 = "0wfvvp8mv02dfg2zbaxsj2cbc79k4fjr71giw60q5ibmwa6s7wvs";  }
          { url = "https://locus.accur8.net/repos/all/org/hsqldb/hsqldb/2.6.1/hsqldb-2.6.1.jar";  sha256 = "1s6bdmi0irlwlkh0lnahp4k3lv290c6x83n1wmgbk1frfbafkyg3";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-resolver-dns/4.1.82.Final/netty-resolver-dns-4.1.82.Final.jar";  sha256 = "12b914gyjyvlcz97vpn2jmbrjjh2b8gclhbpra9hp9dv1mrkh354";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/http-client-spi/2.19.6/http-client-spi-2.19.6.jar";  sha256 = "17dxqc13vp75r8ada4dfn542ajybh7iabqz7n2c9xikq4bg2b3bl";  }
          { url = "https://locus.accur8.net/repos/all/org/rogach/scallop_2.13/4.1.0/scallop_2.13-4.1.0.jar";  sha256 = "0ya2szbc0pvhx1wdmwdscxyhzmax772fvrjdvx54ncpwjk6gd629";  }
          { url = "https://locus.accur8.net/repos/all/org/typelevel/jawn-ast_2.13/1.3.2/jawn-ast_2.13-1.3.2.jar";  sha256 = "0zls1k5z3rwq11k940p87nn8psf0gj3xssagi17nmv1pf6ldrrm9";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-codec-http2/4.1.86.Final/netty-codec-http2-4.1.86.Final.jar";  sha256 = "0rxi3n0ghkdzgarixg10nk7qiws54297hiwhxnd9ifxnda7f5s78";  }
          { url = "https://locus.accur8.net/repos/all/org/scala-lang/modules/scala-xml_2.13/1.2.0/scala-xml_2.13-1.2.0.jar";  sha256 = "1lvg0l84jmzhzwls6zpplrrl8if7sxr1vymbsnhx3mdy81w2ng91";  }
          { url = "https://locus.accur8.net/repos/all/org/typelevel/cats-parse_2.13/0.3.8/cats-parse_2.13-0.3.8.jar";  sha256 = "1gnkm5n7h7a57bd1p0qxffk93spaw4h8s7ln8h14c75xaz1fnkwh";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/izumi-reflect_2.13/2.2.2/izumi-reflect_2.13-2.2.2.jar";  sha256 = "01ms5z761zglcj03gi0fc7w5jkvs371qmwdppgcdz2pdr0h4jdgl";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-logging-slf4j_2.13/2.1.5/zio-logging-slf4j_2.13-2.1.5.jar";  sha256 = "098xkbg33qghfkr6s5ckd3dd1k4m0dpziwrs363v8dzpvmbqmd8r";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/regions/2.19.6/regions-2.19.6.jar";  sha256 = "1acy3440fmjzq9a0wi5pmx6w19c9jpgh0sfygjiv65z45g7bh30r";  }
          { url = "https://locus.accur8.net/repos/all/com/github/alexarchambault/argonaut-shapeless_6.2_2.13/1.2.0-M11/argonaut-shapeless_6.2_2.13-1.2.0-M11.jar";  sha256 = "0awgadhjxs542bkqnhgv5grm33nxbwgdn20ycs0i07g0swrnlbmx";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-transport-native-epoll/4.1.82.Final/netty-transport-native-epoll-4.1.82.Final-linux-x86_64.jar";  sha256 = "1ryv5pvqqacq6qb0v447a97ihl736gg240qhiwhz8q7vh9h1yrfl";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-codec-http/4.1.86.Final/netty-codec-http-4.1.86.Final.jar";  sha256 = "110bqhibncw3qdj3zwlksmadnmyf40913dayaiq7pkyg28qynv1z";  }
          { url = "https://locus.accur8.net/repos/all/io/get-coursier/coursier_2.13/2.0.0-RC6/coursier_2.13-2.0.0-RC6.jar";  sha256 = "1aknnsxhnj2mkjv28ypn2vdmfm5zwi5ms5mpyl1p2q5b6fp5h8wv";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/auth/2.19.6/auth-2.19.6.jar";  sha256 = "1gl6aazg3w1nmh5dw35rpanx6hbynvkglj116pja6pdygv3a4qxa";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/annotations/2.19.6/annotations-2.19.6.jar";  sha256 = "1plwv5nsqyvryn2jkwf50i2nkvqq0kq8mr490285wlp22zljww77";  }
          { url = "https://locus.accur8.net/repos/all/com/lihaoyi/sourcecode_2.13/0.2.7/sourcecode_2.13-0.2.7.jar";  sha256 = "1lqixz35p0x8pmf60lb913p01lma89acc4z2lpcazfr15l7ajfd6";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-common/4.1.86.Final/netty-common-4.1.86.Final.jar";  sha256 = "16dfqa7k9sj4jysjscza2dib7wsf5mqfg8wsabccaifdwwb3ynm3";  }
          { url = "https://locus.accur8.net/repos/all/ant/ant/1.6.2/ant-1.6.2.jar";  sha256 = "0h513bwlhc6p5rvxix629c09xk8dxsyk61xfc6y9xyprlsgn7vff";  }
          { url = "https://locus.accur8.net/repos/all/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar";  sha256 = "1iyh53li6y4b8gp8bl52fagqp8iqrkp4rmwa5jb8f9izg2hd4skn";  }
          { url = "https://locus.accur8.net/repos/all/org/typelevel/case-insensitive_2.13/1.2.0/case-insensitive_2.13-1.2.0.jar";  sha256 = "0k92bw1w3mxc412xhyn93pmcgxsylnivdkwwz7xvmxmi7gh5zgkn";  }
          { url = "https://locus.accur8.net/repos/all/org/typelevel/cats-kernel_2.13/2.8.0/cats-kernel_2.13-2.8.0.jar";  sha256 = "1lkm47iz89ma68rhap16miz3mkpwgpxh8j5pgl8nar7nd860hf5y";  }
          { url = "https://locus.accur8.net/repos/all/io/accur8/a8-versions_2.13/1.0.0-20230406_2144_master/a8-versions_2.13-1.0.0-20230406_2144_master.jar";  sha256 = "1acnp94j8llk98mzs24s8wry81ygyg3nlrpvii12i7aq6z10as64";  }
          { url = "https://locus.accur8.net/repos/all/org/slf4j/slf4j-jdk14/2.0.5/slf4j-jdk14-2.0.5.jar";  sha256 = "1hy0nfar4j00lddikfmjha0kqbgz7xihmmxdg1ak4fsmdbvlm7hr";  }
          { url = "https://locus.accur8.net/repos/all/org/apache/httpcomponents/httpcore/4.4.13/httpcore-4.4.13.jar";  sha256 = "091lnk100aqdg2bwbyg8rnp64dyfpz6kgicylg7my92317a8jvp0";  }
          { url = "https://locus.accur8.net/repos/all/com/softwaremill/sttp/client3/armeria-backend_2.13/3.8.5/armeria-backend_2.13-3.8.5.jar";  sha256 = "0ma3akjf97jjfpcpah0vbdrcgmqgksb4cpam1gk7gjfl02y02pi5";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-resolver-dns-classes-macos/4.1.82.Final/netty-resolver-dns-classes-macos-4.1.82.Final.jar";  sha256 = "0d3phw5g2vygxf1xsvkswx5zd5f04272k0ykn71nwmqllgs7d1ds";  }
          { url = "https://locus.accur8.net/repos/all/io/get-coursier/coursier-cache_2.13/2.0.0-RC6/coursier-cache_2.13-2.0.0-RC6.jar";  sha256 = "0djw02faggq0hwap4jwdhdmw0bjicz7v26d3kjxlvyc3vfbskgmb";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-handler-proxy/4.1.82.Final/netty-handler-proxy-4.1.82.Final.jar";  sha256 = "1xwd65dj1y5zqzblvjnrpa2lbs4bshajkp0q3z1j3h1n8njasxhp";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/json-utils/2.19.6/json-utils-2.19.6.jar";  sha256 = "00y4rbnahsa24asm8l2d0qbccvld70m26wdw0vr33lm1smzalgdz";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-prelude-macros_2.13/1.0.0-RC16/zio-prelude-macros_2.13-1.0.0-RC16.jar";  sha256 = "110c05dq7lhvhbhk8jl4fkxp74zp6nqz6zc25xa9hxw3la973dmp";  }
          { url = "https://locus.accur8.net/repos/all/com/sun/mail/jakarta.mail/2.0.1/jakarta.mail-2.0.1.jar";  sha256 = "08ycjh20wgz0h88g827pchxqy9fj7lwj77hpnwyigvi2x6yvv249";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-transport/4.1.86.Final/netty-transport-4.1.86.Final.jar";  sha256 = "17rch35xh0p5iw664qlan07a6w1s8ixldx5lnd32p4p4ak6nswpn";  }
          { url = "https://locus.accur8.net/repos/all/org/fusesource/jansi/jansi/1.18/jansi-1.18.jar";  sha256 = "16r2ynk177hk26zjrcwxnfiv01zifsfp0m6n7cd7lz3ncpy697hh";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/metrics-spi/2.19.6/metrics-spi-2.19.6.jar";  sha256 = "1q1y7j94h93mdim6l5mgm5dn6ma8nr806isp8wph2j7izhllvnik";  }
          { url = "https://locus.accur8.net/repos/all/com/softwaremill/sttp/client3/armeria-backend-zio_2.13/3.8.5/armeria-backend-zio_2.13-3.8.5.jar";  sha256 = "0sii8zf2d82flbvzzgsaq6b7f2k1xskllwbk557h7783wsahgrkg";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-logging_2.13/2.1.5/zio-logging_2.13-2.1.5.jar";  sha256 = "19wswxwbk6p6w265b14vz5qn3624smarysrg4y4ckl41vwn5z0iw";  }
          { url = "https://locus.accur8.net/repos/all/io/argonaut/argonaut_2.13/6.2.3/argonaut_2.13-6.2.3.jar";  sha256 = "1zn9ri3cr7yvd6k998r9xqcd5f8ak04m8ppykswbky07qfm9z8cz";  }
          { url = "https://locus.accur8.net/repos/all/io/micrometer/micrometer-core/1.9.4/micrometer-core-1.9.4.jar";  sha256 = "1f7cx1qan4d20vcqp5lckwx9yk0nl69vxvmsqpqjxrc9clhp42lb";  }
          { url = "https://locus.accur8.net/repos/all/org/scala-lang/modules/scala-collection-compat_2.13/2.7.0/scala-collection-compat_2.13-2.7.0.jar";  sha256 = "1misjha2yz4642r93r94i3r21qkmga9yz4zp9vrk9ii211ki0rck";  }
          { url = "https://locus.accur8.net/repos/all/com/aayushatharva/brotli4j/brotli4j/1.8.0/brotli4j-1.8.0.jar";  sha256 = "0fqa3pqwsar1rrcdpalz1b8zbf40jkmpscl91frs3nzhqf5niq6b";  }
          { url = "https://locus.accur8.net/repos/all/com/softwaremill/sttp/shared/zio_2.13/1.3.10/zio_2.13-1.3.10.jar";  sha256 = "0k5wygqlpc56jrg86fydpwgih88i3k84wb97w4hlr81d5jrq5392";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-transport-classes-epoll/4.1.86.Final/netty-transport-classes-epoll-4.1.86.Final.jar";  sha256 = "1npda5dpk59waj2kqa1bdis8v7zh5nlp58cnsprlnssxv23ypirw";  }
          { url = "https://locus.accur8.net/repos/all/com/fasterxml/jackson/core/jackson-core/2.13.4/jackson-core-2.13.4.jar";  sha256 = "1krr13ym32q613955qc84fzsb13pq7aqndzwlrxyxngd00r08bjc";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio_2.13/2.0.5/zio_2.13-2.0.5.jar";  sha256 = "1qifg7g1njadn3s8ycy6hyfbg68yb90yrbr2xcjba1zykc1jymi7";  }
          { url = "https://locus.accur8.net/repos/all/org/postgresql/postgresql/42.3.4/postgresql-42.3.4.jar";  sha256 = "1bj04890ic0jzx7spjld3vw7c4vs3hcdv9aja64rxwa5crqxapjx";  }
          { url = "https://locus.accur8.net/repos/all/com/softwaremill/sttp/client3/core_2.13/3.8.5/core_2.13-3.8.5.jar";  sha256 = "0ds1s7g6ibsajpv3v12kil3gixqa6sdf4q12g9zcv87y4vaj0shp";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-process_2.13/0.7.1/zio-process_2.13-0.7.1.jar";  sha256 = "05nfq37d3wc3zpcvwk7gg5zva30p8l2p6gb7xssmkn42419zmbbc";  }
          { url = "https://locus.accur8.net/repos/all/com/softwaremill/sttp/client3/zio_2.13/3.8.5/zio_2.13-3.8.5.jar";  sha256 = "0kbpnh6av7dk9b6i442y4r47bpyjzz71mpxlz03xx44815fr3qhz";  }
          { url = "https://locus.accur8.net/repos/all/com/github/andyglow/typesafe-config-scala_2.13/2.0.0/typesafe-config-scala_2.13-2.0.0.jar";  sha256 = "1zp716vj9rp12g8d7fxf6v631c6g7mmxq5jmmwmscfyg9gdj2iya";  }
          { url = "https://locus.accur8.net/repos/all/org/typelevel/cats-core_2.13/2.8.0/cats-core_2.13-2.8.0.jar";  sha256 = "0shl38abiywr6mcdw3vfj7ck45fsqn0l6s2ypqpz100wfx1b55rk";  }
          { url = "https://locus.accur8.net/repos/all/com/fasterxml/jackson/datatype/jackson-datatype-jdk8/2.13.4/jackson-datatype-jdk8-2.13.4.jar";  sha256 = "0vk05v1jmi41bq3v69j9mj95fm7g4nncb4rds59xfi080998hmpw";  }
          { url = "https://locus.accur8.net/repos/all/io/github/alexarchambault/windows-ansi/windows-ansi/0.0.1/windows-ansi-0.0.1.jar";  sha256 = "12xdlv0lpvs4rc9m93dfp8ffrf9bx3305fpny5b28swlc3h9nbl9";  }
          { url = "https://locus.accur8.net/repos/all/commons-codec/commons-codec/1.11/commons-codec-1.11.jar";  sha256 = "0zbv3psvznwx23iwirphvy0qhkpsdmz958in47s4ialpiqqxb6g5";  }
          { url = "https://locus.accur8.net/repos/all/org/typelevel/jawn-util_2.13/1.3.2/jawn-util_2.13-1.3.2.jar";  sha256 = "048rzicznzvk0nammm5gwv8ikrrybgp6bzm0n2mb4wc65n1jsdx8";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/protocol-core/2.19.6/protocol-core-2.19.6.jar";  sha256 = "14si82i7vqbmrpg4fcykd8qvz952lr6fy9dqdjjgdb46vc26jzzy";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-tcnative-boringssl-static/2.0.54.Final/netty-tcnative-boringssl-static-2.0.54.Final-linux-x86_64.jar";  sha256 = "0hfm7r8pw04c2yi535lmc8h1zbnjhirhi5dcy4b6fvcv1sw0pxa1";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-handler/4.1.86.Final/netty-handler-4.1.86.Final.jar";  sha256 = "0kf0w7xssjbk4bw7jn5mhia04m3wvxyif99fabf7ici954ll56z6";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-codec-haproxy/4.1.82.Final/netty-codec-haproxy-4.1.82.Final.jar";  sha256 = "1bjz896ijjyp19hwrac66npqnl061g8l908j9rfhh5hrk3fc302z";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-tcnative-boringssl-static/2.0.54.Final/netty-tcnative-boringssl-static-2.0.54.Final-osx-aarch_64.jar";  sha256 = "0sbqvw0nwv8dxxcpdlbf3cwnpnydjxzlqw3iz3jq19niy975bm98";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-codec-dns/4.1.82.Final/netty-codec-dns-4.1.82.Final.jar";  sha256 = "19n78szc577zyxwiwrm0v35izbxd64n58vwv6i35y9yjgdq80gs6";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-cache_2.13/0.2.1/zio-cache_2.13-0.2.1.jar";  sha256 = "0c861v40za2n81cyrmh9m2r3bc6g8a69rgbknqbnb3lql0c6nghz";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/netty-nio-client/2.19.6/netty-nio-client-2.19.6.jar";  sha256 = "1r92jv12w52v1p9p6bxgraskbaxpdh6nyhraha02hlf4pp158npn";  }
          { url = "https://locus.accur8.net/repos/all/io/get-coursier/coursier-util_2.13/2.0.0-RC6/coursier-util_2.13-2.0.0-RC6.jar";  sha256 = "0rw980bwfxrxzhpj4f2m92bbnd8nb5sz7m2zb15f76658rqsywsh";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-transport-native-unix-common/4.1.86.Final/netty-transport-native-unix-common-4.1.86.Final-linux-x86_64.jar";  sha256 = "04yjq9kmwd0zi7wh1ndr1pgl2lbfmyh1rn59zc4y5v97f126bv6z";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/aws-xml-protocol/2.19.6/aws-xml-protocol-2.19.6.jar";  sha256 = "0dzv0wn6k71gzsym73lm1h38ajpsil940cj8hlmgbvi22b8a4fl7";  }
          { url = "https://locus.accur8.net/repos/all/org/checkerframework/checker-qual/3.5.0/checker-qual-3.5.0.jar";  sha256 = "19556pzrqcwwmavx3yrbnm6b9kcbjnv3cf2pq9pn15cay6rr16bj";  }
          { url = "https://locus.accur8.net/repos/all/org/scala-lang/scala-library/2.13.10/scala-library-2.13.10.jar";  sha256 = "1hdivpz604rk4xnnzgz82viric4b3z778cxg73xfh0yf7xy61jp6";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/third-party-jackson-core/2.19.6/third-party-jackson-core-2.19.6.jar";  sha256 = "1jdk37g7gfvlxr0dr1w8ypx4pyl3bcc6ph5czaqlj985jxpb5z53";  }
          { url = "https://locus.accur8.net/repos/all/org/reactivestreams/reactive-streams/1.0.4/reactive-streams-1.0.4.jar";  sha256 = "0a2z6ag51d4gb82kbnvjls7scd0h5snbjmqqyrcaqgcvg2bsap7p";  }
          { url = "https://locus.accur8.net/repos/all/com/sun/activation/jakarta.activation/2.0.1/jakarta.activation-2.0.1.jar";  sha256 = "0w986gsfpgfvibcdzqbq9nx6vj9h66z32rd95rb9ax70srylpqmr";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-stacktracer_2.13/2.0.5/zio-stacktracer_2.13-2.0.5.jar";  sha256 = "1gj78cg9dc8v01bs6kvyc6sf82c85giib761wrp1c70bwllzjni9";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/profiles/2.19.6/profiles-2.19.6.jar";  sha256 = "0j1z2i4j2s387jawiasxfph51zngykx6iwk8vhrhj77l0mqs7d79";  }
          { url = "https://locus.accur8.net/repos/all/commons-logging/commons-logging/1.2/commons-logging-1.2.jar";  sha256 = "0dm61zgmgjkg67kf9dyrzgpayd18r656n05kiabmc3xyl0gfmpfs";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/izumi-reflect-thirdparty-boopickle-shaded_2.13/2.2.2/izumi-reflect-thirdparty-boopickle-shaded_2.13-2.2.2.jar";  sha256 = "17swxadihdapyxdr9y0b25l40524w3gy0vnfa76jh5yp352kaddv";  }
          { url = "https://locus.accur8.net/repos/all/ch/qos/logback/logback-core/1.2.10/logback-core-1.2.10.jar";  sha256 = "024s763qdwanp84k9iq24aisljmbqdrllbklzvbrs7v9avza6lds";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-streams_2.13/2.0.5/zio-streams_2.13-2.0.5.jar";  sha256 = "07yhzld0fzpjbnnbzy2gkmsq87094ngaxx66djxixbj6cnri8xc5";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-tcnative-boringssl-static/2.0.54.Final/netty-tcnative-boringssl-static-2.0.54.Final-linux-aarch_64.jar";  sha256 = "0ppdv85w6p54pdz4bygk2vza69qfi6cgn4s9zmlig9wgkbrh3j2c";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-interop-reactivestreams_2.13/2.0.0/zio-interop-reactivestreams_2.13-2.0.0.jar";  sha256 = "09541b9vzvmp98449qrmjhnsqkfmblfawn2ng2033c09vv120gi1";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/aws-query-protocol/2.19.6/aws-query-protocol-2.19.6.jar";  sha256 = "139jrfzlkm8bniripghfy94by0g4xcby2b8jjhkkc96nvpm3hcmi";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/endpoints-spi/2.19.6/endpoints-spi-2.19.6.jar";  sha256 = "0i9yc2szqhyxbfm9lkgn2kjyg0v4kf821rrmmpcd9bd7bsj75881";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-codec/4.1.86.Final/netty-codec-4.1.86.Final.jar";  sha256 = "1sjv681nv6hxvx2q2141bnv6v4axmnlx2748mdnas7c5bh5q8mh4";  }
          { url = "https://locus.accur8.net/repos/all/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.13.4/jackson-datatype-jsr310-2.13.4.jar";  sha256 = "1xzc9nw586fn5rdvwby9ys1qrbrs74gqx8jxj9qp5hdc1chmvmss";  }
          { url = "https://locus.accur8.net/repos/all/dev/zio/zio-prelude_2.13/1.0.0-RC16/zio-prelude_2.13-1.0.0-RC16.jar";  sha256 = "1syks6lp4bly0nfxv0v8y9ywykk8xx2bzz41d48kridvxfcph9yi";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/aws-core/2.19.6/aws-core-2.19.6.jar";  sha256 = "05wd8pizpj926zbd234cl1cwzhjhxfyp1j51k4xk9rgdr3ip8n08";  }
          { url = "https://locus.accur8.net/repos/all/com/chuusai/shapeless_2.13/2.3.3/shapeless_2.13-2.3.3.jar";  sha256 = "1zrxd3b475cw07l30jnd2z964sx00qdqwbrh0y48f7np6xjdws8m";  }
          { url = "https://locus.accur8.net/repos/all/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar";  sha256 = "0ghwrm1vi7b6pfdi95z1cpxpy69m4a6112drcw0fal7z6zsws350";  }
          { url = "https://locus.accur8.net/repos/all/org/wvlet/airframe/airframe-log_2.13/22.1.0/airframe-log_2.13-22.1.0.jar";  sha256 = "0yghwdzg812rf3bl5s2bbs9cqjqnc3ar8bq8lq2m8c5ri1y7v4y4";  }
          { url = "https://locus.accur8.net/repos/all/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar";  sha256 = "0hzp3vrxbnyc6w86v671wp0zchb634rgrwwcc00m0skcarm05sbg";  }
          { url = "https://locus.accur8.net/repos/all/org/typelevel/jawn-parser_2.13/1.3.2/jawn-parser_2.13-1.3.2.jar";  sha256 = "0p6nv0zc34ajymxgkz34kbv9k5a96w6nr71nbfbcfdjlh1m17v8l";  }
          { url = "https://locus.accur8.net/repos/all/org/scalameta/fastparse_2.13/1.0.1/fastparse_2.13-1.0.1.jar";  sha256 = "13ni9wj19v3d8bf9iph4qqkjgp2h70xhhrqlmy6r8lav9lj9jfxl";  }
          { url = "https://locus.accur8.net/repos/all/com/google/protobuf/protobuf-java/3.11.4/protobuf-java-3.11.4.jar";  sha256 = "1wm348581f061nh96aba8g1zwb8gi29x2bacfgylj6ixymc8zsa2";  }
          { url = "https://locus.accur8.net/repos/all/com/softwaremill/sttp/shared/ws_2.13/1.3.10/ws_2.13-1.3.10.jar";  sha256 = "0jbk8bz6ssgw4zn4hfybpkiax3a90b2mzwf47y0n6j2y90hq4szk";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-resolver-dns-native-macos/4.1.82.Final/netty-resolver-dns-native-macos-4.1.82.Final-osx-aarch_64.jar";  sha256 = "0kv5c35i0i5zn9ijr9fzsgx3gzydfcj7i4vjm7lc8sk7blbqhky1";  }
          { url = "https://locus.accur8.net/repos/all/com/zaxxer/HikariCP/4.0.3/HikariCP-4.0.3.jar";  sha256 = "1jf6i1vfk8ihgzigh5sdca77si76klzm2ls4sxv3a1n1y7pll0kw";  }
          { url = "https://locus.accur8.net/repos/all/com/typesafe/config/1.4.1/config-1.4.1.jar";  sha256 = "13wl9iwx4vp6ikhf60z53q51861irpa87h8zqi08hp674giaf2jc";  }
          { url = "https://locus.accur8.net/repos/all/org/hdrhistogram/HdrHistogram/2.1.12/HdrHistogram-2.1.12.jar";  sha256 = "1ql1f9bj6gxcw8n8r7f65clbrr398llymw04gr5srsjg8jpgniwv";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/sdk-core/2.19.6/sdk-core-2.19.6.jar";  sha256 = "1jn8mych9a1mjm175nhw1jvppr4c4imjs56glh9sbc9bd5x4sl1v";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/awssdk/apache-client/2.19.6/apache-client-2.19.6.jar";  sha256 = "17nn1b5670mkbs2zxv67540iy8z2ky98zh0rshvc3iv0c559p5w6";  }
          { url = "https://locus.accur8.net/repos/all/io/accur8/a8-sync-shared_2.13/1.0.0-20230108_1051_master/a8-sync-shared_2.13-1.0.0-20230108_1051_master.jar";  sha256 = "1i7syv16y9cxkkynfkmb8h1x9pdxip46wc50630cg5xfcj66bgp6";  }
          { url = "https://locus.accur8.net/repos/all/io/get-coursier/coursier-core_2.13/2.0.0-RC6/coursier-core_2.13-2.0.0-RC6.jar";  sha256 = "1cjh2csr8dwyx8z53zixfl65clqhzcp6lwd6a42g7l9z7ch5s77d";  }
          { url = "https://locus.accur8.net/repos/all/org/scalameta/fastparse-utils_2.13/1.0.1/fastparse-utils_2.13-1.0.1.jar";  sha256 = "03rlvrzj6wmm8vlpr963715swk2sfzzwbd4bh156hdiqj11harcx";  }
          { url = "https://locus.accur8.net/repos/all/software/amazon/eventstream/eventstream/1.0.1/eventstream-1.0.1.jar";  sha256 = "08p8dk0p7v39lb1k9kig87x21syhn08826qr0b1h4zqijvkdhdqc";  }
          { url = "https://locus.accur8.net/repos/all/com/beachape/enumeratum-macros_2.13/1.6.1/enumeratum-macros_2.13-1.6.1.jar";  sha256 = "1xdp4d40fxw5xm2xizy40gdkc5szf26xx4zfzpyhdcliz5ykimrr";  }
          { url = "https://locus.accur8.net/repos/all/org/scalactic/scalactic_2.13/3.2.10/scalactic_2.13-3.2.10.jar";  sha256 = "17hrg668gazxpawkxvkr757752ppvjwf2qa85gzjlpnr5ahr983k";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-tcnative-classes/2.0.54.Final/netty-tcnative-classes-2.0.54.Final.jar";  sha256 = "1d6f5bdcr6iybqzfg7a6qvziq2yjnyjmxlhfdy0qwxv6k8kpx5wq";  }
          { url = "https://locus.accur8.net/repos/all/io/netty/netty-buffer/4.1.86.Final/netty-buffer-4.1.86.Final.jar";  sha256 = "125y9ayiv26lxlbnd7mfykwxrnkxzpmkiq56zaqncll6gks1abp4";  }
          { url = "https://locus.accur8.net/repos/all/net/sf/jt400/jt400/10.7/jt400-10.7.jar";  sha256 = "172j27mr0d5kf837zqillqkdcb070n6gjh6qib1k1migv1alfvk5";  }
          { url = "https://locus.accur8.net/repos/all/com/linecorp/armeria/armeria/1.20.3/armeria-1.20.3.jar";  sha256 = "1y99cv1ci3gnjf1w9jwmi33yd131k10zd0yrk1z51raacawlha9j";  }
        ];
      };

    webappExplode = if launcherConfig.webappExplode == null then false else launcherConfig.webappExplode;

    fetcherFn = 
      dep: (
        fetchurl {
          url = dep.url;
          sha256 = dep.sha256;
        }
      );

    javaVersion = launcherConfig.javaVersion;

    jdk = 
      if javaVersion == null then jdk11
      else if javaVersion == "8" then jdk8
      else if javaVersion == "11" then jdk11
      else if javaVersion == "17" then jdk17
      else abort("expected javaVersion = [ 8 | 11 | 17 ] got ${javaVersion}")
    ;

    artifacts = map fetcherFn launcherConfig.dependencies;

    linkFarmEntryFn = drv: { name = drv.name; path = drv; };

    classpathBuilder = linkFarm launcherConfig.name (map linkFarmEntryFn artifacts);

    args = builtins.concatStringsSep " " (launcherConfig.jvmArgs ++ [launcherConfig.mainClass] ++ launcherConfig.args);

    webappExploder = 
      if webappExplode then
        ''
          echo exploding webapp-composite folder
          for jar in ${classpathBuilder}/*.jar
          do
            ${unzip}/bin/unzip $jar "webapp/*" -d $out/webapp-composite 2> /dev/null 1> /dev/null || true
          done
        ''
      else
        ""
    ;

  in

    stdenv.mkDerivation {
      name = launcherConfig.name;
      src = ./.;
      installPhase = ''

        mkdir -p $out/bin

        # create link to jdk bin so that top and other tools show the process name as something meaningful
        ln -s ${jdk}/bin/java $out/bin/${launcherConfig.name}j

        # create link to lib folder derivation
        ln -s ${classpathBuilder} $out/lib

        LAUNCHER=$out/bin/${launcherConfig.name}

        # setup launcher script
        cp ./java-launcher-template $LAUNCHER
        chmod +x $LAUNCHER
        substituteInPlace $LAUNCHER \
          --replace _name_ ${launcherConfig.name} \
          --replace _out_ $out \
          --replace _args_ "${args}"

      '' + webappExploder;
    }
