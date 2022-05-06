package com.laysan.autojob.modules.cloud189;

import lombok.Data;

/**
 * { "result":0,
 * "msg":"登录成功",
 * "toUrl":"https://cloud.189.cn/api/portal/callbackUnify.action?redirectURL=https://cloud.189.cn/web/redirect.html&appId=cloud&paras=8F7A09BCF7EDDA63EADF62E03A7DF5B1B545C76BDAEC932FD3413D8038A7C82D385593E6809E2A226F5904AA272BCD6BF820A8D0200E4DF882F9AE108919D96D2CF35BC8596F53255FF74477D47CDC0073EE18D4B96DB2DD98A59187720685CD696A710099C5C5141982F106C89BE1E7C75228C4A02F6EC886D9DACE838667A676CBC8D82AC65DA6E25E8CB5C6CDCD96A3D15F0F36A05713411F84B086738C91D29AD1913B87D3F8021EE9A343336010698A727B04938285B8FC24CF95EDC81066B6CCA16119CFC668E65D6493DAFFAA6955DE4306B38C43FF0E0A5C9727B50D23D7206112EBA4FD8A6C5ADFED1428CAC99F4C5366F04DDAEE672CF8DE6F195ED3AE93D58A8071D1B1BA0B31F853D8D4EE3219CA90EA26EEAD6BFCF58DC79DE41355C2093F8F72F4BAB57F306AE8195E9E09840E464A69A6883A74CD9AE550E767F01415E22839000CB40C67237B8A2C06440D801DB105329627C182A9667174B1B9DDABFC7C724D89A12545DC117782D59DFC208D2CEDD2BB276479E885D82DA22FC1400C3BD1EF1F2D14B835C0048A4943F1A4FA74395D631CEAE7BA217BD4D79CC8489DFB154E87D79402BB070EEA284A37F63F422E9CFD7442411494F5C89F678137EFFCC9A02F9BB1A2460CCA39BADCEF3DB483171AEDFA1AC749269CCE425A9FBACAD9FBA9AF3ECC8CE64D061A11B3F79863F24FCCC20391158D50F39B370A7A9F7A30717650E0EF18CBB84185&sign=062DA6303C3B7F05CFCBA61C7C4A4E52BEA5FEEA"
 * }
 */
@Data
public class Cloud189LoginResult {
    private Integer result;
    private String msg;
    private String toUrl;
}
