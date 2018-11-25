steem
		.broadcast(
				[
						[
								'comment',
								{
									'parent_author' : '',
									'parent_permlink' : 'dporn',
									'author' : author,
									'permlink' : permlink,
									'title' : title,
									'body' : body,
									'json_metadata' : JSON
											.stringify({//TODO
												app : 'dporn.app/v0.0.3',
												tags : tags,
												image : [ '"https://steemitimages.com/0x0/https://gateway.ipfs.io/ipfs/'
														+ posterHash + '"' ]
											})
								} ], [ 'comment_options', {
							'author' : author,
							'permlink' : permlink,
							'max_accepted_payout' : '1000000.000 SBD',
							'percent_steem_dollars' : 10000,
							'allow_votes' : true,
							'allow_curation_rewards' : true,
							'extensions' : [ [ 0, {
								'beneficiaries' : ben
							} ] ]
						} ] ], function(err, response) {
				});}
